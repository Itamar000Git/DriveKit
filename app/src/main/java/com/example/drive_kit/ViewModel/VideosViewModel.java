package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.VideosRepository;
import com.example.drive_kit.Model.VideoItem;

import java.util.List;

/**
 * VideosViewModel holds the data for DIY screens and talks to VideosRepository.
 */
public class VideosViewModel extends ViewModel {

    private final VideosRepository repo = new VideosRepository();

    private final MutableLiveData<List<String>> yearRanges = new MutableLiveData<>();
    private final MutableLiveData<List<VideoItem>> issues = new MutableLiveData<>();
    private final MutableLiveData<VideoItem> selectedVideo = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<String>> getYearRanges() { return yearRanges; }
    public LiveData<List<VideoItem>> getIssues() { return issues; }
    public LiveData<VideoItem> getSelectedVideo() { return selectedVideo; }
    public LiveData<String> getError() { return error; }

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

}
