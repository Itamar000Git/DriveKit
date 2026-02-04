package com.example.drive_kit.Data.Repository;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InsuranceInquiryRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void logInquiry(String userId, String companyId, String companyName) {
        if (userId == null || companyId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("companyId", companyId);
        data.put("companyName", companyName);
        data.put("type", "OPEN_DETAILS");
        data.put("source", "DriveKit");
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("insurance_inquiries").add(data);
    }
}
