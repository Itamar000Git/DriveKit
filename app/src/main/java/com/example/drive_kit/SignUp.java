package com.example.drive_kit;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import Model.Driver;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;




//class that represent the signup page
public class SignUp extends AppCompatActivity {

    private Button next;
    private EditText firstNameEditText;

    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText carNumberEditText;
    private EditText insuranceDateEditText;


//in this page we take the client details and create a driver object
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);


        //find the text fields and the button
        next = findViewById(R.id.next);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        carNumberEditText = findViewById(R.id.carNumberEditText);
        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);

        //listening to the signup button
        next.setOnClickListener(v -> {

            //  extract the text from the text fields
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String carNumber = carNumberEditText.getText().toString().trim();
            String insuranceDate = insuranceDateEditText.getText().toString().trim();


            Intent intent = new Intent(SignUp.this, set_username_password.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("email", email);
            intent.putExtra("carNumber", carNumber);
            intent.putExtra("insuranceDate", insuranceDate);


            startActivity(intent);
            // Logcat
        });

    }

}
