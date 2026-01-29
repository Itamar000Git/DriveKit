package com.example.drive_kit.Model;

/**
 * All possible faults in the DIY screen.
 * We keep a Hebrew label for the UI buttons.
 */
public enum FaultType {
    AC("מזגן"),
    CABIN_FILTER("פילטר פנים רכב"),
    ENGINE_FILTER("פילטר מנוע"),
    BATTERY("מצבר"),
    BLUETOOTH("בלוטוס"),
    BRAKE_FLUID("נוזל בלמים"),
    SEATS("מושבים"),
    ENGINE_LIGHTS("נורות מנוע"),
    CLOCK("שעון"),
    COOLANT("נוזל קירור"),
    ENGINE_FUSE("פיוז מנוע"),
    CABIN_FUSE("פיוז פנים רכב"),
    HOOD("מכסה מנוע"),
    OIL("שמן"),
    POWER_STEERING_FLUID("נוזל הגה"),
    WINDSHIELD_WASHER("נוזל ניקוי שמשה"),
    WIPERS("מגבים");

    private final String hebrewLabel;

    FaultType(String hebrewLabel) {
        this.hebrewLabel = hebrewLabel;
    }

    public String getLabel() {
        return hebrewLabel;
    }
}
