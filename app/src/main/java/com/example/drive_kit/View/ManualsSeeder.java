package com.example.drive_kit.View;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ManualsSeeder {

    public static void seedIfNeeded(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences sp = context.getSharedPreferences("seed", Context.MODE_PRIVATE);
        if (sp.getBoolean("manuals_seed_done", false)) return;

        try {
            String json = readAsset(context, "manuals_seed.json");
            JSONArray arr = new JSONArray(json);

            WriteBatch batch = db.batch();
            CollectionReference col = db.collection("manuals_seed");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject manufacturerObj = arr.getJSONObject(i);
                String manufacturer = manufacturerObj.getString("manufacturer");

                // שומרים את כל האובייקט במסמך של היצרן
                Map<String, Object> data = jsonObjectToMap(manufacturerObj);
                DocumentReference doc = col.document(manufacturer);
                batch.set(doc, data);
            }

            batch.commit().addOnSuccessListener(v -> {
                sp.edit().putBoolean("manuals_seed_done", true).apply();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readAsset(Context context, String filename) throws IOException {
        InputStream is = context.getAssets().open(filename);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = is.read(buffer)) > 0) bos.write(buffer, 0, n);
        is.close();
        return bos.toString("UTF-8");
    }

    // המרות בסיסיות כדי לשמור ב-Firestore (Map/List)
    private static Map<String, Object> jsonObjectToMap(JSONObject obj) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            Object v = obj.get(k);
            map.put(k, jsonToJava(v));
        }
        return map;
    }

    private static Object jsonToJava(Object v) throws JSONException {
        if (v == JSONObject.NULL) return null;
        if (v instanceof JSONObject) return jsonObjectToMap((JSONObject) v);
        if (v instanceof JSONArray) {
            JSONArray a = (JSONArray) v;
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < a.length(); i++) list.add(jsonToJava(a.get(i)));
            return list;
        }
        return v; // String / Number / Boolean
    }
}
