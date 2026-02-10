//package com.example.drive_kit.ViewModel;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.drive_kit.Data.Repository.VideosRepository;
//import com.example.drive_kit.Model.Car; // ✅ [STEP2] ADDED
//import com.example.drive_kit.Model.VideoItem;
//
//import java.util.List;
//
//public class VideosViewModel extends ViewModel {
//
//    private final VideosRepository repo = new VideosRepository();
//
//    private final MutableLiveData<List<String>> yearRanges = new MutableLiveData<>();
//    private final MutableLiveData<List<VideoItem>> issues = new MutableLiveData<>();
//    private final MutableLiveData<VideoItem> selectedVideo = new MutableLiveData<>();
//    private final MutableLiveData<String> error = new MutableLiveData<>();
//
//    // ✅ [STEP2] ADDED
//    private final MutableLiveData<Car> myCar = new MutableLiveData<>();
//    private final MutableLiveData<String> myCarError = new MutableLiveData<>();
//
//    public LiveData<List<String>> getYearRanges() { return yearRanges; }
//    public LiveData<List<VideoItem>> getIssues() { return issues; }
//    public LiveData<VideoItem> getSelectedVideo() { return selectedVideo; }
//    public LiveData<String> getError() { return error; }
//
//    // ✅ [STEP2] ADDED
//    public LiveData<Car> getMyCar() { return myCar; }
//    public LiveData<String> getMyCarError() { return myCarError; }
//
//    public void loadYearRanges(String manufacturer, String model) {
//        repo.getYearRanges(manufacturer, model, new VideosRepository.ResultCallback<List<String>>() {
//            @Override public void onSuccess(List<String> data) { yearRanges.setValue(data); }
//            @Override public void onError(Exception e) { error.setValue(e.getMessage()); }
//        });
//    }
//
//    public void loadIssues(String manufacturer, String model, String yearRange) {
//        repo.getIssues(manufacturer, model, yearRange, new VideosRepository.ResultCallback<List<VideoItem>>() {
//            @Override public void onSuccess(List<VideoItem> data) { issues.setValue(data); }
//            @Override public void onError(Exception e) { error.setValue(e.getMessage()); }
//        });
//    }
//
//    public void loadVideo(String manufacturer, String model, String yearRange, String issueKey) {
//        repo.getVideo(manufacturer, model, yearRange, issueKey, new VideosRepository.ResultCallback<VideoItem>() {
//            @Override public void onSuccess(VideoItem data) { selectedVideo.setValue(data); }
//            @Override public void onError(Exception e) { error.setValue(e.getMessage()); }
//        });
//    }
//
//    public void seedDatabaseFromAssets(android.content.Context context) {
//        repo.seedVideosFromAssets(context, new VideosRepository.ResultCallback<Integer>() {
//            @Override public void onSuccess(Integer count) {
//                error.setValue("Seed done. Uploaded: " + count);
//            }
//            @Override public void onError(Exception e) {
//                error.setValue("Seed failed: " + e.getMessage());
//            }
//        });
//    }
//
//    // ✅ [STEP2] ADDED
//    public void loadMyCar() {
//        myCarError.setValue(null);
//
//        repo.getMyCar(new VideosRepository.ResultCallback<Car>() {
//            @Override public void onSuccess(Car data) { myCar.setValue(data); }
//            @Override public void onError(Exception e) {
//                myCarError.setValue(e.getMessage());
//                myCar.setValue(null);
//            }
//        });
//    }
//}


package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.ManualsRepository;
import com.example.drive_kit.Data.Repository.VideosRepository;
import com.example.drive_kit.Model.Car;
import com.example.drive_kit.Model.VideoItem;

import java.util.List;

public class VideosViewModel extends ViewModel {

    private final VideosRepository repo = new VideosRepository();

    // NEW
    private final ManualsRepository manualsRepo = new ManualsRepository();
    private final MutableLiveData<String> manualPdfUrl = new MutableLiveData<>();
    private final MutableLiveData<String> manualError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> manualLoading = new MutableLiveData<>(false);

    private final MutableLiveData<List<String>> yearRanges = new MutableLiveData<>();
    private final MutableLiveData<List<VideoItem>> issues = new MutableLiveData<>();
    private final MutableLiveData<VideoItem> selectedVideo = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final MutableLiveData<Car> myCar = new MutableLiveData<>();
    private final MutableLiveData<String> myCarError = new MutableLiveData<>();

    public LiveData<List<String>> getYearRanges() { return yearRanges; }
    public LiveData<List<VideoItem>> getIssues() { return issues; }
    public LiveData<VideoItem> getSelectedVideo() { return selectedVideo; }
    public LiveData<String> getError() { return error; }

    public LiveData<Car> getMyCar() { return myCar; }
    public LiveData<String> getMyCarError() { return myCarError; }

    // NEW
    public LiveData<String> getManualPdfUrl() { return manualPdfUrl; }
    public LiveData<String> getManualError() { return manualError; }
    public LiveData<Boolean> getManualLoading() { return manualLoading; }

    public void loadYearRanges(String manufacturer, String model) {
        repo.getYearRanges(manufacturer, model, new VideosRepository.ResultCallback<List<String>>() {
            @Override public void onSuccess(List<String> data) { yearRanges.setValue(data); }
            @Override public void onError(Exception e) { error.setValue(e.getMessage()); }
        });
    }

    public void loadIssues(String manufacturer, String model, String yearRange) {
        repo.getIssues(manufacturer, model, yearRange, new VideosRepository.ResultCallback<List<VideoItem>>() {
            @Override public void onSuccess(List<VideoItem> data) { issues.setValue(data); }
            @Override public void onError(Exception e) { error.setValue(e.getMessage()); }
        });
    }

    public void loadVideo(String manufacturer, String model, String yearRange, String issueKey) {
        repo.getVideo(manufacturer, model, yearRange, issueKey, new VideosRepository.ResultCallback<VideoItem>() {
            @Override public void onSuccess(VideoItem data) { selectedVideo.setValue(data); }
            @Override public void onError(Exception e) { error.setValue(e.getMessage()); }
        });
    }

    public void seedDatabaseFromAssets(android.content.Context context) {
        repo.seedVideosFromAssets(context, new VideosRepository.ResultCallback<Integer>() {
            @Override public void onSuccess(Integer count) {
                error.setValue("Seed done. Uploaded: " + count);
            }
            @Override public void onError(Exception e) {
                error.setValue("Seed failed: " + e.getMessage());
            }
        });
    }

    public void loadMyCar() {
        myCarError.setValue(null);

        repo.getMyCar(new VideosRepository.ResultCallback<Car>() {
            @Override public void onSuccess(Car data) { myCar.setValue(data); }
            @Override public void onError(Exception e) {
                myCarError.setValue(e.getMessage());
                myCar.setValue(null);
            }
        });
    }

    // NEW: load manual URL based on current filter
    public void loadManual(String manufacturer, String model, String yearRangeLabel) {
        manualError.setValue(null);
        manualPdfUrl.setValue(null);
        manualLoading.setValue(true);

        int[] yr = parseYearRange(yearRangeLabel);
        int from = yr[0];
        int to = yr[1];

        manualsRepo.getManualDownloadUrl(manufacturer, model, from, to,
                new ManualsRepository.ResultCallback<String>() {
                    @Override public void onSuccess(String url) {
                        manualLoading.setValue(false);
                        manualPdfUrl.setValue(url);
                    }

                    @Override public void onError(Exception e) {
                        manualLoading.setValue(false);
                        manualError.setValue(e.getMessage());
                        manualPdfUrl.setValue(null);
                    }
                });
    }

    private int[] parseYearRange(String label) {
        try {
            String s = (label == null) ? "" : label.trim().replace(" ", "");
            String[] parts = s.split("-");
            if (parts.length != 2) return new int[]{-1, -1};
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            return new int[]{from, to};
        } catch (Exception e) {
            return new int[]{-1, -1};
        }
    }
}
