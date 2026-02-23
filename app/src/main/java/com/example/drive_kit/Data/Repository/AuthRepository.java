package com.example.drive_kit.Data.Repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class AuthRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }
}
