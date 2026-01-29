package com.example.drive_kit.Model;

import java.util.Locale;

public enum CarModel {
    TOYOTA("טויוטה", "Toyota"),
    MAZDA("מאזדה", "Mazda"),
    HONDA("הונדה", "Honda"),
    BMW("ב.מ.וו", "במוו", "BMW"),
    TESLA("טסלה", "Tesla"),
    HYUNDAY("יונדאי", "HYUNDAI", "Hyundai"),
    UNKNOWN();

    private final String[] aliases;
    CarModel(String... aliases) {
        this.aliases = aliases;
    }

    public static CarModel fromGovValue(String raw) {
        if (raw == null) return UNKNOWN;

        String key = normalize(raw);
        if (key.isEmpty()) return UNKNOWN;

        for (CarModel m : values()) {
            if (m == UNKNOWN) continue;

            // 1) Match enum name itself (rare for Hebrew, but ok)
            String enumKey = normalize(m.name());
            if (enumKey.equals(key) || key.contains(enumKey) || enumKey.contains(key)) {
                return m;
            }

            // 2) Match any alias (Hebrew/English variants) - exact OR contains
            for (String a : m.aliases) {
                String aliasKey = normalize(a);
                if (aliasKey.isEmpty()) continue;

                if (aliasKey.equals(key) || key.contains(aliasKey) || aliasKey.contains(key)) {
                    return m;
                }
            }
        }
        return UNKNOWN;
    }


    private static String normalize(String s) {
        return s.trim()
                .toUpperCase(Locale.ROOT)
                // keep only letters/digits (including Hebrew), remove spaces/punctuation
                .replaceAll("[^0-9A-Zא-ת]+", "");
    }
}
