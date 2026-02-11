package com.example.drive_kit.View.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;

public class InsuranceInquiriesAdapter extends RecyclerView.Adapter<InsuranceInquiriesAdapter.VH> {

    public interface OnMarkContactedClick {
        void onClick(String docId);
    }

    private final List<Map<String, Object>> items;
    private final OnMarkContactedClick markCallback;

    // NEW: האם להציג בכלל את כפתור "סומן כטופל"
    private final boolean showMarkButton;

    public InsuranceInquiriesAdapter(
            List<Map<String, Object>> items,
            OnMarkContactedClick markCallback,
            boolean showMarkButton
    ) {
        this.items = items;
        this.markCallback = markCallback;
        this.showMarkButton = showMarkButton;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_insurance_inquiry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Map<String, Object> m = items.get(position);

        String name = safe(m.get("driverName"));
        String phone = safe(m.get("driverPhone"));
        String email = safe(m.get("driverEmail"));
        String carNumber = safe(m.get("carNumber"));
        String carModel = safe(m.get("carModel"));
        String message = safe(m.get("message"));
        String status = safe(m.get("status"));
        String docId = safe(m.get("docId"));

        h.nameText.setText("שם: " + name);
        h.phoneText.setText("טלפון: " + phone);
        h.emailText.setText("אימייל: " + email);
        h.carText.setText("רכב: " + carModel + " | " + carNumber);
        h.messageText.setText("הודעה: " + message);
        h.statusText.setText("סטטוס: " + (status.isEmpty() ? "new" : status));

        // אם זו רשימת "פניות שטופלו" -> מסתירים לגמרי את הכפתור
        if (!showMarkButton) {
            h.contactedButton.setVisibility(View.GONE);
            h.contactedButton.setOnClickListener(null);
            return;
        }

        // אחרת (פניות חדשות): הכפתור מוצג, ומנוטרל אם כבר contacted
        h.contactedButton.setVisibility(View.VISIBLE);

        boolean alreadyContacted = "contacted".equalsIgnoreCase(status);
        h.contactedButton.setEnabled(!alreadyContacted);

        h.contactedButton.setOnClickListener(v -> {
            if (!docId.isEmpty() && markCallback != null && !alreadyContacted) {
                markCallback.onClick(docId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView nameText, phoneText, emailText, carText, messageText, statusText;
        MaterialButton contactedButton;

        VH(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            phoneText = itemView.findViewById(R.id.phoneText);
            emailText = itemView.findViewById(R.id.emailText);
            carText = itemView.findViewById(R.id.carText);
            messageText = itemView.findViewById(R.id.messageText);
            statusText = itemView.findViewById(R.id.statusText);
            contactedButton = itemView.findViewById(R.id.contactedButton);
        }
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString().trim();
    }
}
