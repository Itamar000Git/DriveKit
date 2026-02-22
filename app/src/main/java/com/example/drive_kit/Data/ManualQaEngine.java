package com.example.drive_kit.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ManualQaEngine {

    public static class QaHit {
        public final String snippet;
        public final float score;

        public QaHit(String snippet, float score) {
            this.snippet = snippet;
            this.score = score;
        }
    }

    public static class QaResult {
        public final String answer;
        public final List<QaHit> evidence;

        public QaResult(String answer, List<QaHit> evidence) {
            this.answer = answer;
            this.evidence = evidence;
        }
    }

    // Tuning
    private static final int MAX_RESULTS = 5;
    private static final int MIN_BLOCK_LEN = 80;

    // Snippet window (more context)
    private static final int SNIP_BEFORE = 220;
    private static final int SNIP_AFTER  = 380;
    private static final int MAX_SNIP    = 650;

    public static QaResult answer(String question, String manualText) {
        if (isBlank(question)) {
            return new QaResult("Write a question in English so I can search the manual.", Collections.emptyList());
        }
        if (isBlank(manualText)) {
            return new QaResult("The manual is still loading for Q&A. Try again in a moment.", Collections.emptyList());
        }

        // If user typed Hebrew, warn to search in English (manual is English)
        boolean hasHebrew = containsHebrew(question);
        boolean hasLatin = containsLatin(question);

        // Build query variants (smart expansions)
        QueryPlan plan = buildQueryPlan(question);

        List<String> blocks = splitToBlocks(manualText);

        List<ScoredBlock> scored = new ArrayList<>();
        for (String b : blocks) {
            String bNorm = normalize(b);
            if (bNorm.length() < MIN_BLOCK_LEN) continue;

            float score = scoreBlock(plan, bNorm);
            if (score <= 0) continue;

            String snippet = snippetAroundFirstMatch(b, bNorm, plan);
            scored.add(new ScoredBlock(snippet, score));
        }

        if (scored.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("I couldn't find a clear match in the manual.\n");
            if (hasHebrew && !hasLatin) {
                msg.append("Tip: the manual is in English—try searching in English (e.g., \"engine oil\", \"tire pressure\", \"fuse\", \"battery\").");
            } else {
                msg.append("Try keywords like: \"engine oil\", \"oil level\", \"dipstick\", \"tire pressure\", \"warning light\", \"fuse\", \"battery\", \"towing\".");
            }
            return new QaResult(msg.toString(), Collections.emptyList());
        }

        scored.sort((a, b) -> Float.compare(b.score, a.score));

        int totalMatches = scored.size();
        List<QaHit> top = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_RESULTS, scored.size()); i++) {
            top.add(new QaHit(scored.get(i).snippet, scored.get(i).score));
        }

        StringBuilder ans = new StringBuilder();

        // Header
        ans.append("Found ").append(totalMatches).append(" relevant section(s) for: ")
                .append(plan.displayQuery).append("\n\n");

        if (hasHebrew && !hasLatin) {
            ans.append("Note: The manual is in English. For best results, search in English.\n\n");
        }

        // Best hit
        ans.append("Top result:\n");
        ans.append("“").append(top.get(0).snippet).append("”");

        if (top.size() > 1) {
            ans.append("\n\nMore results:\n");
            for (int i = 1; i < top.size(); i++) {
                ans.append("• ").append(top.get(i).snippet).append("\n");
            }
        }

        return new QaResult(ans.toString().trim(), top);
    }

    // ---------------- Query Plan ----------------

    private static class QueryPlan {
        final String displayQuery;
        final Set<String> tokens;
        final List<String> phrases;

        QueryPlan(String displayQuery, Set<String> tokens, List<String> phrases) {
            this.displayQuery = displayQuery;
            this.tokens = tokens;
            this.phrases = phrases;
        }
    }

    private static QueryPlan buildQueryPlan(String questionRaw) {
        String qTrim = questionRaw == null ? "" : questionRaw.trim();

        // Normalize and tokenize
        String qNorm = normalize(qTrim);
        Set<String> tokens = tokenize(qNorm);

        // Phrase list (stronger signals)
        List<String> phrases = new ArrayList<>();
        if (!isBlank(qNorm)) phrases.add(qNorm);

        // Smart expansions: if user typed Hebrew oil-ish, or "oil"
        // We keep expansions in EN because manual is EN.
        if (looksLikeOilQuery(qTrim)) {
            addPhrase(phrases, "engine oil");
            addPhrase(phrases, "oil level");
            addPhrase(phrases, "check engine oil");
            addPhrase(phrases, "oil change");
            addPhrase(phrases, "oil filter");
            addPhrase(phrases, "dipstick");
            addPhrase(phrases, "viscosity");
            addPhrase(phrases, "sae");
            addPhrase(phrases, "0w");
            addPhrase(phrases, "5w");
            addPhrase(phrases, "10w");
            // also tokens
            tokens.add("engine");
            tokens.add("oil");
            tokens.add("dipstick");
            tokens.add("viscosity");
            tokens.add("sae");
        }

        // If user asked in Hebrew (general), we don't translate (no server),
        // but we do encourage English and keep their raw tokens.
        // Still, we can add a small general EN fallback for common topics:
        if (containsHebrew(qTrim) && !containsLatin(qTrim)) {
            // no aggressive expansion besides oil; keep it safe.
        }

        String display = qTrim;
        if (looksLikeOilQuery(qTrim)) {
            display = "\"oil\" / engine oil / dipstick / oil level";
        }

        return new QueryPlan(display, tokens, phrases);
    }

    private static void addPhrase(List<String> phrases, String p) {
        if (p == null) return;
        String n = normalize(p);
        if (!isBlank(n) && !phrases.contains(n)) phrases.add(n);
    }

    private static boolean looksLikeOilQuery(String q) {
        if (q == null) return false;
        String lower = q.toLowerCase(Locale.ROOT);
        // Hebrew signals + english
        return lower.contains("oil")
                || q.contains("שמן")
                || q.contains("שמן מנוע")
                || q.contains("מנוע") && q.contains("שמן");
    }

    // ---------------- Scoring ----------------

    private static class ScoredBlock {
        final String snippet;
        final float score;

        ScoredBlock(String snippet, float score) {
            this.snippet = snippet;
            this.score = score;
        }
    }

    private static float scoreBlock(QueryPlan plan, String blockNorm) {
        float score = 0f;

        // Phrase bonus (strong)
        for (String ph : plan.phrases) {
            if (isBlank(ph)) continue;
            if (ph.length() >= 4 && blockNorm.contains(ph)) score += 4.0f;
        }

        // Token matches
        int matched = 0;
        for (String tok : plan.tokens) {
            if (tok.length() < 3) continue;
            if (blockNorm.contains(tok)) {
                matched++;
                score += 1.0f;
            }
        }

        // Bonus for more matches
        if (matched >= 4) score += 1.5f;
        if (matched >= 7) score += 2.5f;

        return score;
    }

    // ---------------- Snippet with more context ----------------

    private static String snippetAroundFirstMatch(String originalBlock, String blockNorm, QueryPlan plan) {
        if (isBlank(originalBlock)) return "";

        // Find best “anchor” (prefer phrase, then token)
        int idx = -1;

        for (String ph : plan.phrases) {
            if (isBlank(ph) || ph.length() < 4) continue;
            idx = indexOfIgnoreCase(originalBlock, ph);
            if (idx >= 0) break;
        }

        if (idx < 0) {
            for (String tok : plan.tokens) {
                if (tok.length() < 3) continue;
                idx = indexOfIgnoreCase(originalBlock, tok);
                if (idx >= 0) break;
            }
        }

        // Fallback: start of block
        if (idx < 0) idx = 0;

        int start = Math.max(0, idx - SNIP_BEFORE);
        int end = Math.min(originalBlock.length(), idx + SNIP_AFTER);

        String snip = originalBlock.substring(start, end).trim().replaceAll("\\s+", " ");

        if (snip.length() > MAX_SNIP) {
            snip = snip.substring(0, MAX_SNIP).trim() + "…";
        }

        if (start > 0) snip = "… " + snip;
        if (end < originalBlock.length()) snip = snip + " …";

        return snip;
    }

    private static int indexOfIgnoreCase(String text, String needleNorm) {
        if (text == null || needleNorm == null) return -1;
        String tNorm = normalize(text);
        int i = tNorm.indexOf(needleNorm);
        if (i < 0) return -1;

        // Approximate mapping back: use original lower() search for better anchor
        String lowerText = text.toLowerCase(Locale.ROOT);
        String lowerNeedle = needleNorm.toLowerCase(Locale.ROOT);
        return lowerText.indexOf(lowerNeedle);
    }

    // ---------------- Block split ----------------

    private static List<String> splitToBlocks(String text) {
        String[] raw = text.split("(\\r?\\n){2,}");
        List<String> out = new ArrayList<>();
        for (String r : raw) {
            String t = r.trim();
            if (!t.isEmpty()) out.add(t);
        }

        if (out.size() < 10) {
            out.clear();
            String[] sentences = text.split("(?<=[\\.\\!\\?\\:])\\s+");
            StringBuilder buf = new StringBuilder();
            for (String s : sentences) {
                if (buf.length() > 0) buf.append(" ");
                buf.append(s.trim());
                if (buf.length() >= 450) {
                    out.add(buf.toString().trim());
                    buf.setLength(0);
                }
            }
            if (buf.length() > 0) out.add(buf.toString().trim());
        }

        return out;
    }

    // ---------------- Text utils ----------------

    private static Set<String> tokenize(String s) {
        if (isBlank(s)) return Collections.emptySet();
        String[] parts = s.split("\\s+");
        Set<String> out = new HashSet<>();
        for (String p : parts) {
            String t = p.trim();
            if (t.length() >= 2) out.add(t);
        }
        return out;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[^0-9a-zא-ת]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean containsHebrew(String s) {
        if (s == null) return false;
        return s.matches(".*[א-ת].*");
    }

    private static boolean containsLatin(String s) {
        if (s == null) return false;
        return s.matches(".*[a-zA-Z].*");
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
