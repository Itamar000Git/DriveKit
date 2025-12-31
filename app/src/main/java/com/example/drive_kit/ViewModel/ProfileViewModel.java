package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.ProfileRepository;
import com.example.drive_kit.Model.Driver;
/**
 * ProfileViewModel belongs to the Profile screen.
 *
 * This ViewModel is responsible for:
 * - Loading the current user's Driver data from the repository
 * - Exposing the Driver data to the UI using LiveData
 * - Exposing an error message if something fails
 *
 * IMPORTANT:
 * This class does NOT touch UI elements directly (no TextViews, no Activities).
 * It only provides data and events that the UI can observe.
 */
public class ProfileViewModel extends ViewModel {
    // Repository that knows how to fetch the Driver from the database (Firestore)
    private final ProfileRepository repo = new ProfileRepository();

    // LiveData that holds the Driver object (profile data)
    // The Activity/Fragment observes this to display the user's details
    private final MutableLiveData<Driver> driver = new MutableLiveData<>();
    // LiveData that holds an error message if loading fails
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Expose the Driver LiveData to the UI (read-only from outside)
    public LiveData<Driver> getDriver() { return driver; }

    // Expose the error message LiveData to the UI (read-only from outside)
    public LiveData<String> getErrorMessage() { return errorMessage; }

    /**
     * Loads the profile for the given user ID (uid).
     *
     * Flow:
     * 1) Ask the repository to fetch the Driver document for this uid.
     * 2) Repository returns the result asynchronously using a callback:
     *    - onSuccess(Driver d): publish the Driver to LiveData
     *    - onError(Exception e): publish an error message to LiveData
     *
     * @param uid Firebase user ID (the document id in "drivers" collection)
     */
    public void loadProfile(String uid) {
        // Call the repository method to fetch the driver's data (asynchronous call)
        repo.getDriver(uid, new ProfileRepository.DriverCallback() {
            @Override
            public void onSuccess(Driver d) {
                // Update LiveData with the loaded Driver object.
                // postValue is safe if the callback runs on a background thread.
                driver.postValue(d);
            }

            @Override
            // If something went wrong (network, permission, missing doc, etc.),
            // publish an error message so the UI can show a Toast or message.
            public void onError(Exception e) {
                errorMessage.postValue("שגיאה בטעינת הנתונים");
            }
        });
    }
}
