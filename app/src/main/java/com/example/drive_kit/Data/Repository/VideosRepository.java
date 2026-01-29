package com.example.drive_kit.Data.Repository;

import android.content.Context;

import com.example.drive_kit.Model.VideoItem;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * VideosRepository talks to Firestore collection: "videos".
 *
 * It supports:
 * 1) Get distinct yearRange values for (manufacturer, model)
 * 2) Get distinct issues list for (manufacturer, model, yearRange)
 * 3) Get a single video by full filter
 * 4) (DEV) Seed Firestore from a JSON file in assets (run manually when needed)
 */
public class VideosRepository {

    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Get distinct yearRange values for (manufacturer, model).
     * Example output: ["2014-2018", "2019-2022"]
     */
    public void getYearRanges(String manufacturer, String model, ResultCallback<List<String>> cb) {
        db.collection("videos")
                .whereEqualTo("manufacturer", manufacturer)
                .whereEqualTo("model", model)
                .get()
                .addOnSuccessListener(snapshot -> {
                    HashSet<String> set = new HashSet<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String yr = doc.getString("yearRange");
                        if (yr != null && !yr.trim().isEmpty()) set.add(yr.trim());
                    }

                    ArrayList<String> list = new ArrayList<>(set);
                    Collections.sort(list);
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Get all issues (distinct) for (manufacturer, model, yearRange).
     * We return one VideoItem per issueKey, so the UI can show buttons with issueNameHe.
     */
    public void getIssues(String manufacturer, String model, String yearRange, ResultCallback<List<VideoItem>> cb) {
        db.collection("videos")
                .whereEqualTo("manufacturer", manufacturer)
                .whereEqualTo("model", model)
                .whereEqualTo("yearRange", yearRange)
                .get()
                .addOnSuccessListener(snapshot -> {
                    HashSet<String> seen = new HashSet<>();
                    ArrayList<VideoItem> result = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        VideoItem item = doc.toObject(VideoItem.class);
                        if (item == null) continue;

                        String key = item.getIssueKey();
                        if (key == null || key.trim().isEmpty()) continue;

                        if (!seen.contains(key)) {
                            seen.add(key);
                            result.add(item);
                        }
                    }
                    cb.onSuccess(result);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Get a single video doc for the chosen issue.
     */
    public void getVideo(String manufacturer, String model, String yearRange, String issueKey, ResultCallback<VideoItem> cb) {
        db.collection("videos")
                .whereEqualTo("manufacturer", manufacturer)
                .whereEqualTo("model", model)
                .whereEqualTo("yearRange", yearRange)
                .whereEqualTo("issueKey", issueKey)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        cb.onSuccess(null);
                        return;
                    }
                    VideoItem item = snapshot.getDocuments().get(0).toObject(VideoItem.class);
                    cb.onSuccess(item);
                })
                .addOnFailureListener(cb::onError);
    }

    /* =========================================================
       DEV ONLY: Seed Firestore from assets/videos_seed.json
       ========================================================= */

    /**
     * Uploads a JSON file from assets into Firestore collection "videos".
     *
     * Requirements:
     * - Put file in: app/src/main/assets/videos_seed.json
     * - JSON must be an array of objects that match VideoItem fields:
     *   manufacturer, model, yearRange, issueKey, issueNameHe, url
     *
     * Behavior:
     * - Uses stable docId, so you can run it again without duplicates (it overwrites).
     * - Runs in one batch commit (up to 500 docs per batch in Firestore).
     */
    public void seedVideosFromAssets(Context context, ResultCallback<Integer> cb) {
        try {
            String json = readAssetFile(context, "videos_seed.json");

            Gson gson = new Gson();
            Type listType = new TypeToken<List<VideoItem>>() {}.getType();
            List<VideoItem> items = gson.fromJson(json, listType);

            if (items == null || items.isEmpty()) {
                cb.onSuccess(0);
                return;
            }

            // Firestore batch limit is 500 writes, so we split if needed
            int totalUploaded = 0;
            int i = 0;

            while (i < items.size()) {
                WriteBatch batch = db.batch();
                int batchCount = 0;

                while (i < items.size() && batchCount < 500) {
                    VideoItem item = items.get(i);
                    i++;

                    if (!isValid(item)) continue;

                    String docId = buildStableDocId(item);
                    DocumentReference ref = db.collection("videos").document(docId);

                    // Overwrite if exists (safe re-run)
                    batch.set(ref, item);
                    batchCount++;
                }

                int finalBatchCount = batchCount;
                int finalUploadedSoFar = totalUploaded;

                // Commit this batch
                batch.commit()
                        .addOnSuccessListener(v -> {
                            // Only report success when the last batch is done.
                            // (Simple approach: call cb at the end of each batch; ok for dev.)
                            cb.onSuccess(finalUploadedSoFar + finalBatchCount);
                        })
                        .addOnFailureListener(cb::onError);

                totalUploaded += batchCount;
            }

        } catch (Exception e) {
            cb.onError(e);
        }
    }

    // --- Helpers for seed ---

    private String readAssetFile(Context context, String fileName) throws Exception {
        InputStream is = context.getAssets().open(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    /**
     * Stable ID so running seed again won't create duplicates.
     * Example: TOYOTA_COROLLA_2016-2020_AC
     */
    private String buildStableDocId(VideoItem item) {
        return normalizeId(item.getManufacturer()) + "_" +
                normalizeId(item.getModel()) + "_" +
                normalizeId(item.getYearRange()) + "_" +
                normalizeId(item.getIssueKey());
    }

    private String normalizeId(String s) {
        if (s == null) return "";
        return s.trim()
                .toUpperCase(Locale.ROOT)
                // keep letters/digits/_/-
                .replaceAll("[^0-9A-Z_\\-]+", "");
    }

    private boolean isValid(VideoItem item) {
        if (item == null) return false;
        return !isBlank(item.getManufacturer())
                && !isBlank(item.getModel())
                && !isBlank(item.getYearRange())
                && !isBlank(item.getIssueKey())
                && !isBlank(item.getUrl());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
