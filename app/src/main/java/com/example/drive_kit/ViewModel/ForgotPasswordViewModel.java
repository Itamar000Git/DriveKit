package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.AuthRepository;

public class ForgotPasswordViewModel extends ViewModel {

    public static class UiState {
        public final boolean loading;
        public final String message;
        public UiState(boolean loading, String message) {
            this.loading = loading;
            this.message = message;
        }
    }

    private final AuthRepository repo = new AuthRepository();
    private final MutableLiveData<UiState> state =
            new MutableLiveData<>(new UiState(false, null));

    public LiveData<UiState> getState() { return state; }

    public void sendReset(String emailRaw) {
        String email = emailRaw == null ? "" : emailRaw.trim();


        final String neutral = "אם החשבון קיים – נשלח מייל לאיפוס סיסמה.";

        if (email.isEmpty()) {
            state.setValue(new UiState(false, "נא להזין אימייל."));
            return;
        }

        state.setValue(new UiState(true, null));
        repo.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> state.setValue(new UiState(false, neutral)))
                .addOnFailureListener(e -> state.setValue(new UiState(false, neutral)));
    }
}
