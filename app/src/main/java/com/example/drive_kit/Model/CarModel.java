package com.example.drive_kit.Model;

import static com.example.drive_kit.Model.CarModel.getYearRangesFor;

import java.util.Locale;

public enum CarModel {
    TOYOTA("טויוטה", "Toyota"),
    MAZDA("מאזדה", "Mazda"),
    HONDA("הונדה", "Honda"),
    HYUNDAI("יונדאי", "HYUNDAI", "Hyundai"),
    UNKNOWN();

    private final String[] aliases;

    CarModel(String... aliases) {
        this.aliases = aliases;
    }

    /**
     * Converts a raw value from the Gov API (often Hebrew) to our enum.
     */
    public static CarModel fromGovValue(String raw) {
        if (raw == null) return UNKNOWN;

        String key = normalize(raw);
        if (key.isEmpty()) return UNKNOWN;

        for (CarModel m : values()) {
            if (m == UNKNOWN) continue;

            // 1) Match enum name
            String enumKey = normalize(m.name());
            if (enumKey.equals(key) || key.contains(enumKey) || enumKey.contains(key)) {
                return m;
            }

            // 2) Match any alias (Hebrew/English variants)
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

    /**
     * Returns the "model enum values" for the selected manufacturer.
     * Used to fill the Model dropdown in the DIY screen.
     */
    public static Enum<?>[] getModelsFor(CarModel manufacturer) {
        switch (manufacturer) {
            case TOYOTA:
                return ToyotaModel.values();
            case MAZDA:
                return MazdaModel.values();
            case HONDA:
                return HondaModel.values();
            case HYUNDAI:
                return HyundaiModel.values();
            default:
                return GenericModel.values();
        }
    }

    /**
     * Returns 2 year ranges for a selected model (as you requested).
     * selectedModelName is the enum name shown in the dropdown.
     */
    public static YearRange[] getYearRangesFor(CarModel manufacturer, String selectedModelName) {
        if (selectedModelName == null) return new YearRange[]{YearRange.UNKNOWN, YearRange.UNKNOWN};

        try {
            switch (manufacturer) {
                case TOYOTA:
                    return ToyotaModel.valueOf(selectedModelName).getYearRanges();
                case MAZDA:
                    return MazdaModel.valueOf(selectedModelName).getYearRanges();
                case HONDA:
                    return HondaModel.valueOf(selectedModelName).getYearRanges();
                case HYUNDAI:
                    return HyundaiModel.valueOf(selectedModelName).getYearRanges();
                default:
                    return GenericModel.valueOf(selectedModelName).getYearRanges();
            }
        } catch (Exception e) {
            return new YearRange[]{YearRange.UNKNOWN, YearRange.UNKNOWN};
        }
    }


    public static int[][] getYearRangesIntFor(CarModel manufacturer, String selectedModelName) {
        YearRange[] ranges = getYearRangesFor(manufacturer, selectedModelName);
        int[][] out = new int[ranges.length][2];
        for (int i = 0; i < ranges.length; i++) {
            YearRange r = ranges[i];
            out[i][0] = (r == null) ? -1 : r.fromYear;
            out[i][1] = (r == null) ? -1 : r.toYear;
        }
        return out;
    }

    public static int[] pickRangeForYear(CarModel manufacturer, String selectedModelName, int year) {
        int[][] ranges = getYearRangesIntFor(manufacturer, selectedModelName);
        for (int[] r : ranges) {
            if (r == null || r.length < 2) continue;
            int from = r[0], to = r[1];
            if (from <= 0 || to <= 0) continue;
            if (year >= from && year <= to) return new int[]{from, to};
        }
        return new int[]{-1, -1};
    }

}

/* =========================
   Year ranges (2 per model)
   ========================= */

enum YearRange {
//    RANGE_2010_2015("2010-2015", 2010, 2015),
//    RANGE_2016_2020("2016-2020", 2016, 2020),
//    RANGE_2021_2024("2021-2024", 2021, 2024),
    RANGE_2014_2018("2014-2018", 2014, 2018),
    RANGE_2019_2024("2019-2024", 2019, 2024),
    RANGE_2015_2017("2015-2017", 2015, 2017),
    RANGE_2018_2024("2018-2024", 2018, 2024),
    RANGE_2013_2016("2013-2016", 2013, 2016),
    RANGE_2017_2024("2017-2024", 2017, 2024),
    RANGE_2016_2021("2016-2021", 2016, 2021),
    RANGE_2022_2024("2022-2024", 2022, 2024),
    RANGE_2007_2017("2007-2017", 2007, 2017),
    RANGE_2010_2015("2010-2015", 2010, 2015),
    RANGE_2017_2022("2017-2022", 2017, 2022),
    RANGE_2023_2024("2023-2024", 2023, 2024),


    UNKNOWN("לא ידוע", -1, -1);

    public final String label;
    public final int fromYear;
    public final int toYear;

    YearRange(String label, int fromYear, int toYear) {
        this.label = label;
        this.fromYear = fromYear;
        this.toYear = toYear;
    }

    @Override
    public String toString() {
        return label; // what the dropdown shows
    }
}

/* =========================
   Models per manufacturer
   2 popular models each
   ========================= */

enum ToyotaModel {
    COROLLA(YearRange.RANGE_2014_2018, YearRange.RANGE_2019_2024),
    CAMRY(YearRange.RANGE_2015_2017, YearRange.RANGE_2018_2024),
    UNKNOWN(YearRange.UNKNOWN, YearRange.UNKNOWN);

    private final YearRange r1;
    private final YearRange r2;

    ToyotaModel(YearRange r1, YearRange r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    public YearRange[] getYearRanges() {
        return new YearRange[]{r1, r2};
    }
}

enum MazdaModel {
    MAZDA3(YearRange.RANGE_2014_2018, YearRange.RANGE_2019_2024),
    CX5(YearRange.RANGE_2013_2016, YearRange.RANGE_2017_2024),
    UNKNOWN(YearRange.UNKNOWN, YearRange.UNKNOWN);

    private final YearRange r1;
    private final YearRange r2;

    MazdaModel(YearRange r1, YearRange r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    public YearRange[] getYearRanges() {
        return new YearRange[]{r1, r2};
    }
}

enum HondaModel {
    CIVIC(YearRange.RANGE_2016_2021, YearRange.RANGE_2022_2024),
    CRV(YearRange.RANGE_2017_2022, YearRange.RANGE_2023_2024),
    UNKNOWN(YearRange.UNKNOWN, YearRange.UNKNOWN);

    private final YearRange r1;
    private final YearRange r2;

    HondaModel(YearRange r1, YearRange r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    public YearRange[] getYearRanges() {
        return new YearRange[]{r1, r2};
    }
}

enum HyundaiModel {
    I10(YearRange.RANGE_2007_2017 ,YearRange.UNKNOWN),
    TUCSON(YearRange.RANGE_2010_2015, YearRange.RANGE_2016_2021),
    UNKNOWN(YearRange.UNKNOWN, YearRange.UNKNOWN);

    private final YearRange r1;
    private final YearRange r2;

    HyundaiModel(YearRange r1, YearRange r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    public YearRange[] getYearRanges() {
        return new YearRange[]{r1, r2};
    }
}

/**
 * Fallback enum for unsupported manufacturer.
 */
enum GenericModel {
    UNKNOWN;

    public YearRange[] getYearRanges() {
        return new YearRange[]{YearRange.UNKNOWN, YearRange.UNKNOWN};
    }
}



