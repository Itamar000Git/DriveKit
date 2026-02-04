package com.example.drive_kit.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.drive_kit.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class InsuranceBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_WEB = "web";
    private static final String ARG_IS_PARTNER = "isPartner";

    public static InsuranceBottomSheet newInstance(
            String id,
            String name,
            String phone,
            String email,
            String web,
            boolean isPartner
    ) {
        InsuranceBottomSheet f = new InsuranceBottomSheet();
        Bundle b = new Bundle();
        b.putString(ARG_NAME, name);
        b.putString(ARG_PHONE, phone);
        b.putString(ARG_EMAIL, email);
        b.putString(ARG_WEB, web);
        b.putBoolean(ARG_IS_PARTNER, isPartner);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_insurance_company, container, false);

        Bundle args = getArguments();
        String name = args != null ? args.getString(ARG_NAME, "") : "";
        String phone = args != null ? args.getString(ARG_PHONE, "") : "";
        String email = args != null ? args.getString(ARG_EMAIL, "") : "";
        String web = args != null ? args.getString(ARG_WEB, "") : "";
        boolean isPartner = args != null && args.getBoolean(ARG_IS_PARTNER, false);

        TextView title = v.findViewById(R.id.bsCompanyName);
        TextView phoneTv = v.findViewById(R.id.bsPhone);
        TextView emailTv = v.findViewById(R.id.bsEmail);
        TextView webTv = v.findViewById(R.id.bsWeb);

        View callBtn = v.findViewById(R.id.bsCallBtn);
        View mailBtn = v.findViewById(R.id.bsMailBtn);
        View webBtn = v.findViewById(R.id.bsWebBtn);

        title.setText(name);
        phoneTv.setText(phone);
        emailTv.setText(email);
        webTv.setText(web);

        callBtn.setOnClickListener(btn -> {
            if (phone == null || phone.trim().isEmpty()) return;
            String encoded = Uri.encode(phone);
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + encoded)));
        });

        mailBtn.setOnClickListener(btn -> {
            if (email == null || email.trim().isEmpty()) return;
            Intent i = new Intent(Intent.ACTION_SENDTO);
            i.setData(Uri.parse("mailto:" + email));
            i.putExtra(Intent.EXTRA_SUBJECT, "פנייה דרך DriveKit");
            startActivity(i);
        });

        webBtn.setOnClickListener(btn -> {
            if (web == null || web.trim().isEmpty()) return;
            String url = web.startsWith("http") ? web : ("https://" + web);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });
        if (phone == null || phone.trim().isEmpty()) callBtn.setVisibility(View.GONE);
        if (email == null || email.trim().isEmpty()) mailBtn.setVisibility(View.GONE);
        if (web == null || web.trim().isEmpty()) webBtn.setVisibility(View.GONE);

        return v;
    }
}
