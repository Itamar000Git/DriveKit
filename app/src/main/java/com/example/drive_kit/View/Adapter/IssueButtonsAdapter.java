package com.example.drive_kit.View.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.Model.VideoItem;
import com.example.drive_kit.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows issues as a list of buttons.
 * Each row is one MaterialButton.
 */
public class IssueButtonsAdapter extends RecyclerView.Adapter<IssueButtonsAdapter.VH> {

    public interface OnIssueClick {
        void onClick(VideoItem item);
    }

    private final List<VideoItem> items = new ArrayList<>();
    private final OnIssueClick listener;

    public IssueButtonsAdapter(OnIssueClick listener) {
        this.listener = listener;
    }

    public void setItems(List<VideoItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_issue_button, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        VideoItem item = items.get(position);

        // Prefer Hebrew display name. If missing, fallback to issueKey.
        String title = (item.getIssueNameHe() != null && !item.getIssueNameHe().trim().isEmpty())
                ? item.getIssueNameHe()
                : item.getIssueKey();

        holder.btn.setText(title);

        holder.btn.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        MaterialButton btn;
        VH(@NonNull View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.issueButton);
        }
    }
}
