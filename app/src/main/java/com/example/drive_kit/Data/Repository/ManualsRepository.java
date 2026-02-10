package com.example.drive_kit.Data.Repository;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class ManualsRepository {

    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    /**
     * Returns a Firebase Storage download URL for the manual that matches:
     * manuals_seed/{MANUFACTURER} -> models[] -> yearRanges[] (from/to) -> pdfPath
     */
    public void getManualDownloadUrl(
            String manufacturer,
            String model,
            int fromYear,
            int toYear,
            ResultCallback<String> cb
    ) {
        if (cb == null) return;

        if (isBlank(manufacturer) || isBlank(model) || fromYear <= 0 || toYear <= 0) {
            cb.onError(new Exception("Invalid manual filter"));
            return;
        }

        db.collection("manuals_seed")
                .document(manufacturer)
                .get()
                .addOnSuccessListener(doc -> {
                    String pdfPath = extractPdfPath(doc, model, fromYear, toYear);
                    if (isBlank(pdfPath)) {
                        cb.onError(new Exception("Manual not found"));
                        return;
                    }

                    StorageReference ref = storage.getReference().child(pdfPath);
                    ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> cb.onSuccess(uri.toString()))
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }

    private String extractPdfPath(DocumentSnapshot doc, String modelName, int from, int to) {
        if (doc == null || !doc.exists()) return null;

        Object modelsObj = doc.get("models");
        if (!(modelsObj instanceof List)) return null;

        List<?> models = (List<?>) modelsObj;

        for (Object mObj : models) {
            if (!(mObj instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) mObj;

            Object modelField = m.get("model");
            if (modelField == null) continue;

            String mName = String.valueOf(modelField).trim();
            if (!mName.equalsIgnoreCase(modelName)) continue;

            Object yearRangesObj = m.get("yearRanges");
            if (!(yearRangesObj instanceof List)) return null;

            List<?> yearRanges = (List<?>) yearRangesObj;

            for (Object yrObj : yearRanges) {
                if (!(yrObj instanceof Map)) continue;
                Map<?, ?> y = (Map<?, ?>) yrObj;

                int yFrom = toInt(y.get("from"));
                int yTo = toInt(y.get("to"));

                if (yFrom == from && yTo == to) {
                    Object pdf = y.get("pdfPath");
                    return (pdf == null) ? null : String.valueOf(pdf);
                }
            }
        }

        return null;
    }

    private int toInt(Object o) {
        try {
            if (o == null) return -1;
            if (o instanceof Number) return ((Number) o).intValue();
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
