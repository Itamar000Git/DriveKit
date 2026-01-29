//package com.example.drive_kit.Model;
//
//import java.util.EnumSet;
//
///**
// * A unique key for (manufacturer + model + year range).
// * Each key holds its own faults list (initially: all faults).
// */
//public enum DiyCarFilterKey {
//
//    // ===== TOYOTA =====
//    TOYOTA_COROLLA_2010_2015(CarModel.TOYOTA, "COROLLA", YearRange.RANGE_2010_2015),
//    TOYOTA_COROLLA_2016_2020(CarModel.TOYOTA, "COROLLA", YearRange.RANGE_2016_2020),
//    TOYOTA_RAV4_2016_2020(CarModel.TOYOTA, "RAV4", YearRange.RANGE_2016_2020),
//    TOYOTA_RAV4_2021_2024(CarModel.TOYOTA, "RAV4", YearRange.RANGE_2021_2024),
//
//    // ===== MAZDA =====
//    MAZDA_MAZDA3_2010_2015(CarModel.MAZDA, "MAZDA3", YearRange.RANGE_2010_2015),
//    MAZDA_MAZDA3_2016_2020(CarModel.MAZDA, "MAZDA3", YearRange.RANGE_2016_2020),
//    MAZDA_CX5_2016_2020(CarModel.MAZDA, "CX5", YearRange.RANGE_2016_2020),
//    MAZDA_CX5_2021_2024(CarModel.MAZDA, "CX5", YearRange.RANGE_2021_2024),
//
//    // ===== HONDA =====
//    HONDA_CIVIC_2010_2015(CarModel.HONDA, "CIVIC", YearRange.RANGE_2010_2015),
//    HONDA_CIVIC_2016_2020(CarModel.HONDA, "CIVIC", YearRange.RANGE_2016_2020),
//    HONDA_CRV_2016_2020(CarModel.HONDA, "CRV", YearRange.RANGE_2016_2020),
//    HONDA_CRV_2021_2024(CarModel.HONDA, "CRV", YearRange.RANGE_2021_2024),
//
//    // ===== HYUNDAY =====
//    HYUNDAY_I10_2010_2015(CarModel.HYUNDAY, "I10", YearRange.RANGE_2010_2015),
//    HYUNDAY_I10_2016_2020(CarModel.HYUNDAY, "I10", YearRange.RANGE_2016_2020),
//    HYUNDAY_TUCSON_2016_2020(CarModel.HYUNDAY, "TUCSON", YearRange.RANGE_2016_2020),
//    HYUNDAY_TUCSON_2021_2024(CarModel.HYUNDAY, "TUCSON", YearRange.RANGE_2021_2024),
//
//    UNKNOWN(CarModel.UNKNOWN, "UNKNOWN", YearRange.UNKNOWN);
//
//    private final CarModel manufacturer;
//    private final String modelName;
//    private final YearRange yearRange;
//
//    // This is the per-triple faults list
//    private final EnumSet<FaultType> faults;
//
//    DiyCarFilterKey(CarModel manufacturer, String modelName, YearRange yearRange) {
//        this.manufacturer = manufacturer;
//        this.modelName = modelName;
//        this.yearRange = yearRange;
//
//        // Initial behavior (as you requested):
//        // Every triple starts with all faults, later you can remove per triple.
//        this.faults = EnumSet.allOf(FaultType.class);
//    }
//
//    public CarModel getManufacturer() { return manufacturer; }
//    public String getModelName() { return modelName; }
//    public YearRange getYearRange() { return yearRange; }
//
//    public EnumSet<FaultType> getFaults() {
//        return EnumSet.copyOf(faults);
//    }
//
//    /**
//     * Find the matching key for a selection.
//     * If nothing matches, returns UNKNOWN.
//     */
//    public static DiyCarFilterKey fromSelection(CarModel manufacturer, String modelName, YearRange range) {
//        if (manufacturer == null || modelName == null || range == null) return UNKNOWN;
//
//        for (DiyCarFilterKey k : values()) {
//            if (k.manufacturer == manufacturer
//                    && k.modelName.equalsIgnoreCase(modelName)
//                    && k.yearRange == range) {
//                return k;
//            }
//        }
//        return UNKNOWN;
//    }
//
//    /**
//     * Later you will use this:
//     * DiyCarFilterKey.TOYOTA_COROLLA_2010_2015.removeFault(FaultType.BLUETOOTH);
//     */
//    public void removeFault(FaultType fault) {
//        if (fault != null) faults.remove(fault);
//    }
//}
//
