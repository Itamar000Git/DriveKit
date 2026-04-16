package com.example.drive_kit.Model;
/**
 * Represents an Insurance Company entity in the system.
 *
 * This model is used to store insurance company data,
 * including contact details, partnership status,
 * and Firestore-related identifiers.
 *
 * Notes:
 * - Some fields are immutable (final) and set via constructor.
 * - Some fields are mutable (e.g., logoUrl, docId, hp) for Firestore updates.
 */
public class InsuranceCompany {

    /** Unique company ID (business identifier) */
    private final String id;

    /** Company display name */
    private final String name;

    /** Contact phone number */
    private final String phone;

    /** Contact email address */
    private final String email;

    /** Company website URL */
    private final String website;

    /** Indicates if this company is a partner in the system */
    private final boolean isPartner;

    /** URL for the company logo (stored in Firebase Storage or web) */
    private String logoUrl;

    /** Firestore document ID (not part of business data, used for DB reference) */
    private String docId;

    /**
     * Company registration number (maps to Firestore field "h_p").
     * Stored as string to preserve formatting and leading zeros if needed.
     */
    private String hp = "";   // maps Firestore field: h_p

    /**
     * @return company registration number (hp)
     */
    public String getHp() { return hp; }

    /**
     * Sets the company registration number.
     * Trims whitespace and prevents null values.
     *
     * @param hp registration number
     */
    public void setHp(String hp) { this.hp = hp == null ? "" : hp.trim(); }

    /**
     * @return Firestore document ID
     */
    public String getDocId() { return docId; }

    /**
     * Sets Firestore document ID.
     *
     * @param docId Firestore document identifier
     */
    public void setDocId(String docId) { this.docId = docId; }

    /**
     * Full constructor for InsuranceCompany.
     *
     * @param id unique company ID
     * @param name company name
     * @param phone phone number
     * @param email email address
     * @param website website URL
     * @param isPartner partnership status
     */
    public InsuranceCompany(String id,
                            String name,
                            String phone,
                            String email,
                            String website,
                            boolean isPartner) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.website = website;
        this.isPartner = isPartner;

        // default
        this.logoUrl = "";
    }

    /** @return company ID */
    public String getId() { return id; }
    /** @return company name */
    public String getName() { return name; }
    /** @return company phone */
    public String getPhone() { return phone; }
    /** @return company email */
    public String getEmail() { return email; }
    /** @return company website */
    public String getWebsite() { return website; }
    /** @return true if company is a partner */
    public boolean isPartner() { return isPartner; }
    /** @return logo URL */
    public String getLogoUrl() { return logoUrl; }

    /**
     * Sets the logo URL.
     * Trims whitespace and prevents null values.
     *
     * @param logoUrl URL string
     */
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = (logoUrl == null) ? "" : logoUrl.trim();
    }
}
