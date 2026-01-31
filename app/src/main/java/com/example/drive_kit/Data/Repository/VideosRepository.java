package com.example.drive_kit.Data.Repository;

import android.content.Context;

import com.example.drive_kit.Model.Car;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.Model.VideoItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * VideosRepository talks to Firestore collection: "videos".
 *
 * It supports:
 * 1) Get distinct yearRange values for (manufacturer, model)
 * 2) Get distinct issues list for (manufacturer, model, yearRange)
 * 3) Get a single video by full filter
 * 4) (DEV) Seed Firestore from a JSON file in assets (run manually when needed)
 */
public class VideosRepository {

    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Get distinct yearRange values for (manufacturer, model).
     * Example output: ["2014-2018", "2019-2022"]
     */
    public void getYearRanges(String manufacturer, String model, ResultCallback<List<String>> cb) {
        db.collection("videos")
                .whereEqualTo("manufacturer", manufacturer)
                .whereEqualTo("model", model)
                .get()
                .addOnSuccessListener(snapshot -> {
                    HashSet<String> set = new HashSet<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String yr = doc.getString("yearRange");
                        if (yr != null && !yr.trim().isEmpty()) set.add(yr.trim());
                    }

                    ArrayList<String> list = new ArrayList<>(set);
                    Collections.sort(list);
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Get all issues (distinct) for (manufacturer, model, yearRange).
     * We return one VideoItem per issueKey, so the UI can show buttons with issueNameHe.
     */
    public void getIssues(String manufacturer, String model, String yearRange, ResultCallback<List<VideoItem>> cb) {
        db.collection("videos")
                .whereEqualTo("manufacturer", manufacturer)
                .whereEqualTo("model", model)
                .whereEqualTo("yearRange", yearRange)
                .get()
                .addOnSuccessListener(snapshot -> {
                    HashSet<String> seen = new HashSet<>();
                    ArrayList<VideoItem> result = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        VideoItem item = doc.toObject(VideoItem.class);
                        if (item == null) continue;

                        String key = item.getIssueKey();
                        if (key == null || key.trim().isEmpty()) continue;

                        if (!seen.contains(key)) {
                            seen.add(key);
                            result.add(item);
                        }
                    }
                    cb.onSuccess(result);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Get a single video doc for the chosen issue.
     */
    public void getVideo(String manufacturer, String model, String yearRange, String issueKey, ResultCallback<VideoItem> cb) {
        db.collection("videos")
                .whereEqualTo("manufacturer", manufacturer)
                .whereEqualTo("model", model)
                .whereEqualTo("yearRange", yearRange)
                .whereEqualTo("issueKey", issueKey)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        cb.onSuccess(null);
                        return;
                    }
                    VideoItem item = snapshot.getDocuments().get(0).toObject(VideoItem.class);
                    cb.onSuccess(item);
                })
                .addOnFailureListener(cb::onError);
    }

    public void seedVideosFromAssets(Context context, ResultCallback<Integer> cb) {
        // Replace-All: delete all docs in "videos" and then upload from JSON
        deleteAllVideos(new ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer deletedCount) {
                uploadVideosFromAssets(context, new ResultCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer uploadedCount) {
                        cb.onSuccess(uploadedCount);
                    }

                    @Override
                    public void onError(Exception e) {
                        cb.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }

/* =========================
   1) DELETE ALL videos docs
   ========================= */

    private void deleteAllVideos(ResultCallback<Integer> cb) {
        // Firestore batch delete limit is 500 operations
        db.collection("videos")
                .limit(500)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        cb.onSuccess(0);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    int count = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        batch.delete(doc.getReference());
                        count++;
                    }

                    int finalCount = count;
                    batch.commit()
                            .addOnSuccessListener(v -> {
                                // Continue deleting next page
                                deleteAllVideos(new ResultCallback<Integer>() {
                                    @Override
                                    public void onSuccess(Integer nextDeleted) {
                                        cb.onSuccess(finalCount + nextDeleted);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        cb.onError(e);
                                    }
                                });
                            })
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }

/* =========================
   2) UPLOAD from assets JSON
   ========================= */

    private void uploadVideosFromAssets(Context context, ResultCallback<Integer> cb) {
        try {
            // IMPORTANT: assets file name only (NO path)
            String json = readAssetFile(context, "videos_seed.json");

            Gson gson = new Gson();
            Type listType = new com.google.gson.reflect.TypeToken<List<VideoItem>>() {}.getType();
            List<VideoItem> items = gson.fromJson(json, listType);

            if (items == null || items.isEmpty()) {
                cb.onSuccess(0);
                return;
            }

            uploadInBatches(items, 0, 0, cb);

        } catch (Exception e) {
            cb.onError(e);
        }
    }

    private void uploadInBatches(List<VideoItem> items, int index, int uploadedSoFar, ResultCallback<Integer> cb) {
        if (index >= items.size()) {
            cb.onSuccess(uploadedSoFar);
            return;
        }

        WriteBatch batch = db.batch();
        int batchCount = 0;
        int i = index;

        while (i < items.size() && batchCount < 500) {
            VideoItem item = items.get(i);
            i++;

            if (!isValid(item)) continue;

            String docId = buildStableDocId(item);
            DocumentReference ref = db.collection("videos").document(docId);

            // set() with stable docId = overwrite safely (but we already deleted all)
            batch.set(ref, item);
            batchCount++;
        }

        final int nextIndex = i;
        final int nextUploaded = uploadedSoFar + batchCount;

        batch.commit()
                .addOnSuccessListener(v -> uploadInBatches(items, nextIndex, nextUploaded, cb))
                .addOnFailureListener(cb::onError);
    }

/* =========================
   Helpers (keep yours)
   ========================= */

    private String readAssetFile(Context context, String fileName) throws Exception {
        InputStream is = context.getAssets().open(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    private String buildStableDocId(VideoItem item) {
        return normalizeId(item.getManufacturer()) + "_" +
                normalizeId(item.getModel()) + "_" +
                normalizeId(item.getYearRange()) + "_" +
                normalizeId(item.getIssueKey());
    }

    private String normalizeId(String s) {
        if (s == null) return "";
        return s.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^0-9A-Z_\\-]+", "");
    }

    private boolean isValid(VideoItem item) {
        if (item == null) return false;
        return !isBlank(item.getManufacturer())
                && !isBlank(item.getModel())
                && !isBlank(item.getYearRange())
                && !isBlank(item.getIssueKey())
                && !isBlank(item.getUrl());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }



    public void getMyCar(ResultCallback<Car> cb) {
        if (cb == null) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            cb.onError(new Exception("User not logged in"));
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        cb.onError(new Exception("Driver document not found"));
                        return;
                    }

                    Driver driver = doc.toObject(Driver.class);
                    if (driver == null) {
                        cb.onError(new Exception("Failed to parse Driver"));
                        return;
                    }

                    Car car = driver.getCar(); // אצלך getCar() עושה ensureCar()
                    if (car == null || car.getCarNum() == null || car.getCarNum().trim().isEmpty()) {
                        cb.onError(new Exception("No car saved for this user"));
                        return;
                    }

                    cb.onSuccess(car);
                })
                .addOnFailureListener(cb::onError);
    }

}
