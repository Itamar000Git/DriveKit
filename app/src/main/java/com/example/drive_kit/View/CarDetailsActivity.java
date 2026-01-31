//package com.example.drive_kit.View;
//
//import android.os.Bundle;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.drive_kit.R;
//
//public class CarDetailsActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_car_details);
//
//        TextView title = findViewById(R.id.carTitle);
//        TextView info  = findViewById(R.id.carInfo);
//
//        String nickname = getIntent().getStringExtra("nickname");
//        String carNumber = getIntent().getStringExtra("carNumber");
//        //String manufacturer = getIntent().getStringExtra("manufacturer");
//        String model = getIntent().getStringExtra("model");
//        int year = getIntent().getIntExtra("year", 0);
//        //long km = getIntent().getLongExtra("km", 0);
//
//        String header = (nickname != null && !nickname.isEmpty()) ? nickname : "פרטי רכב";
//        title.setText(header);
//
//        String text =
//                "לוחית: " + safe(carNumber) + "\n" +
//                        //"יצרן: " + safe(manufacturer) + "\n" +
//                        "דגם: " + safe(model) + "\n" +
//                        "שנה: " + (year == 0 ? "-" : year);
//
//        info.setText(text);
//    }
//
//    private String safe(String s) {
//        return (s == null || s.trim().isEmpty()) ? "-" : s;
//    }
//}


package com.example.drive_kit.View;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CarDetailsActivity extends BaseLoggedInActivity {

    private TextView title;
    private TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_car_details);
        getContentLayoutId();

        title = findViewById(R.id.carTitle);
        info  = findViewById(R.id.carInfo);

        title.setText("פרטי הרכב שלי");

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            info.setText("לא מחובר/ת");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    Driver d = doc.toObject(Driver.class);
                    if (d == null) {
                        info.setText("לא נמצאו נתונים");
                        return;
                    }

                    // רכב יחיד = שדות top-level ב-Driver
                    String carNumber = safe(d.getCar().getCarNum());
                    //String manufacturer = safe(d.getManufacturer());
                    // אם אין לך שדות אלה ב-Driver כרגע – תשאיר "-" או תוריד מהטקסט
                    // String model = safe(d.getCarModel());
                    // int year = d.getYear();
                    // String color = safe(d.getCarColor());

                    String text =
                            "מספר רכב: " + carNumber + "\n" +
                                    "ביטוח: " + safe(d.getFormattedInsuranceDate()) + "\n" +
                                    "טסט: " + safe(d.getFormattedTestDate()) + "\n" +
                                    "טיפול 10K: " + safe(d.getFormattedTreatDate());

                    info.setText(text);
                })
                .addOnFailureListener(e -> info.setText("שגיאה בטעינת נתונים"));
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_car_details;
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }
}
