package com.example.drive_kit;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import Model.Driver;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.datepicker.MaterialDatePicker;

public class SignUp extends AppCompatActivity {

    private Button next;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText carNumberEditText;
    private TextInputEditText insuranceDateEditText;

    // משתנה שישמור את התאריך בפורמט millis
    private long selectedInsuranceDateMillis = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // find views
        next = findViewById(R.id.next);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        carNumberEditText = findViewById(R.id.carNumberEditText);
        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);

        if (insuranceDateEditText == null) {
            Log.e("SignUp", "insuranceDateEditText is NULL (check XML id!)");
        }

        // לחיצה על שדה תאריך → פותח DatePicker פעם אחת בלבד
        insuranceDateEditText.setOnClickListener(v -> openDatePicker());

        // לחיצה על כפתור "הבא"
        next.setOnClickListener(v -> {

            // בדיקה שהתאריך נבחר
            if (selectedInsuranceDateMillis == -1) {
                insuranceDateEditText.setError("בחר תאריך ביטוח");
                return;
            }

            // extract text fields
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String carNumber = carNumberEditText.getText().toString().trim();

            // העברה לדף הבא
            Intent intent = new Intent(SignUp.this, set_username_password.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("email", email);
            intent.putExtra("carNumber", carNumber);
            intent.putExtra("insuranceDateMillis", selectedInsuranceDateMillis);

            startActivity(intent);
        });
    }



    //convert the date to millis
    private void openDatePicker() {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("בחר תאריך ביטוח")
                        .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {

            selectedInsuranceDateMillis = selection;

            insuranceDateEditText.setText(datePicker.getHeaderText());
        });
    }
}
