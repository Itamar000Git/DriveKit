//package com.example.drive_kit.View;
//
//public class InsuranceCompany {
//    private final String id;
//    private final String name;
//    private final String phone;
//    private final String email;
//    private final String website;
//    private final boolean isPartner;
//    private String logoUrl;
//
//    public String getLogoUrl() { return logoUrl; }
//    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
//
//
//    public InsuranceCompany(String id, String name, String phone, String email, String website, boolean isPartner) {
//        this.id = id;
//        this.name = name;
//        this.phone = phone;
//        this.email = email;
//        this.website = website;
//        this.isPartner = isPartner;
//    }
//
//    public String getId() { return id; }
//    public String getName() { return name; }
//    public String getPhone() { return phone; }
//    public String getEmail() { return email; }
//    public String getWebsite() { return website; }
//    public boolean isPartner() { return isPartner; }
//}

package com.example.drive_kit.Model;

public class InsuranceCompany {

    private final String id;
    private final String name;
    private final String phone;
    private final String email;
    private final String website;
    private final boolean isPartner;

    // ✅ NEW
    private String logoUrl;

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

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getWebsite() { return website; }
    public boolean isPartner() { return isPartner; }

    public String getLogoUrl() { return logoUrl; }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = (logoUrl == null) ? "" : logoUrl.trim();
    }
}
