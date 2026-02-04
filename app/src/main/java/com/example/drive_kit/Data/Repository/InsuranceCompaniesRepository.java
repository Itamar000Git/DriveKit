package com.example.drive_kit.Data.Repository;

import com.example.drive_kit.View.InsuranceCompany;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class InsuranceCompaniesRepository {

    public interface Callback {
        void onResult(List<InsuranceCompany> companies);
        void onError(Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void loadCarCompanies(Callback cb) {
        db.collection("insurance_companies")
                .whereEqualTo("category", "car")
                .get()
                .addOnSuccessListener(snap -> {
                    List<InsuranceCompany> list = new ArrayList<>();
                    snap.getDocuments().forEach(doc -> {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        String email = doc.getString("email");
                        String website = doc.getString("website");
                        Boolean isPartner = doc.getBoolean("isPartner");

                        list.add(new InsuranceCompany(
                                id,
                                name == null ? "" : name,
                                phone == null ? "" : phone,
                                email == null ? "" : email,
                                website == null ? "" : website,
                                isPartner != null && isPartner
                        ));
                    });

                    // ✅ מיון מקומי במקום orderBy
                    java.util.Collections.sort(list, (a, b) ->
                            a.getName().compareToIgnoreCase(b.getName())
                    );

                    cb.onResult(list);
                })
                .addOnFailureListener(cb::onError);

    }
}
