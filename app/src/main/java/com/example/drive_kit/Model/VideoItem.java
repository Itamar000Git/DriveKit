package com.example.drive_kit.Model;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentId;

/**
 * VideoItem represents one document in Firestore collection: "videos".
 *
 * We keep the Firestore fields as Strings (so Firestore mapping stays simple),
 * but we also add helper methods to convert to enums when needed.
 *
 * Firestore requires:
 * - public empty constructor
 * - public getters/setters
 */
public class VideoItem {

    // Not saved inside the document fields, but Firestore can inject it
    @DocumentId
    private String docId;

    // Firestore fields (keep as strings exactly like you save in Firestore)
    private String issueKey;      // e.g. "AC"
    private String issueNameHe;   // e.g. "מזגן"
    private String manufacturer;  // e.g. "TOYOTA" (same as enum name)
    private String model;         // e.g. "COROLLA" (same as model enum name)
    private String yearRange;     // e.g. "2014-2018" (same label)
    private String url;           // carcarekiosk link

    public VideoItem() {
        // Firestore empty constructor
    }

    public VideoItem(String issueKey,
                     String issueNameHe,
                     String manufacturer,
                     String model,
                     String yearRange,
                     String url) {
        this.issueKey = issueKey;
        this.issueNameHe = issueNameHe;
        this.manufacturer = manufacturer;
        this.model = model;
        this.yearRange = yearRange;
        this.url = url;
    }

    // --- Document id ---
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    // --- Raw Firestore fields (Strings) ---
    public String getIssueKey() { return issueKey; }
    public void setIssueKey(String issueKey) { this.issueKey = issueKey; }

    public String getIssueNameHe() { return issueNameHe; }
    public void setIssueNameHe(String issueNameHe) { this.issueNameHe = issueNameHe; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getYearRange() { return yearRange; }
    public void setYearRange(String yearRange) { this.yearRange = yearRange; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    // --- Helper conversions (optional but very useful in MVVM/UI) ---

    /**
     * Convert the saved manufacturer string to CarModel enum safely.
     * If the value is invalid -> UNKNOWN.
     */
    public CarModel getManufacturerEnum() {
        if (manufacturer == null) return CarModel.UNKNOWN;
        try {
            return CarModel.valueOf(manufacturer);
        } catch (Exception ignored) {
            return CarModel.UNKNOWN;
        }
    }

    /**
     * Convert the saved yearRange string to YearRange enum (by label).
     * If you use YearRange enum with label like "2014-2018",
     * this method helps you compare/filter safely.
     */
    @Nullable
    public YearRange getYearRangeEnum() {
        if (yearRange == null) return null;
        for (YearRange r : YearRange.values()) {
            if (r == YearRange.UNKNOWN) continue;
            if (yearRange.equals(r.label)) return r;
        }
        return YearRange.UNKNOWN;
    }

    /**
     * UI-friendly title, useful for debugging or list rows.
     */
    public String getDebugTitle() {
        return manufacturer + " " + model + " " + yearRange + " -> " + issueNameHe;
    }
}
