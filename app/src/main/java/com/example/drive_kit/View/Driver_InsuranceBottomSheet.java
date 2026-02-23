package com.example.drive_kit.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.DriverInsuranceSheetViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class Driver_InsuranceBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_HP = "companyIdHp";
    private static final String ARG_DOCID = "companyDocId";

    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_WEB = "web";
    private static final String ARG_IS_PARTNER = "isPartner";

    private DriverInsuranceSheetViewModel vm;

    public static Driver_InsuranceBottomSheet newInstance(
            String companyIdHp,     // h_p (515761625)
            String companyDocId,    // docId (libra)
            String name,
            String phone,
            String email,
            String web,
            boolean isPartner
    ) {
        Driver_InsuranceBottomSheet f = new Driver_InsuranceBottomSheet();
        Bundle b = new Bundle();

        // ✅ הכי חשוב
        b.putString(ARG_HP, companyIdHp);
        b.putString(ARG_DOCID, companyDocId);

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bottomsheet_insurance_company, container, false);

        vm = new ViewModelProvider(this).get(DriverInsuranceSheetViewModel.class);

        vm.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        vm.getSendSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "הפרטים נשלחו לחברת הביטוח", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        Bundle args = getArguments();

        String companyDocId = args != null ? args.getString(ARG_DOCID, "") : "";
        String companyIdHp  = args != null ? args.getString(ARG_HP, "") : "";

        String name  = args != null ? args.getString(ARG_NAME, "") : "";
        String phone = args != null ? args.getString(ARG_PHONE, "") : "";
        String email = args != null ? args.getString(ARG_EMAIL, "") : "";
        String web   = args != null ? args.getString(ARG_WEB, "") : "";

        boolean isPartner = args != null && args.getBoolean(ARG_IS_PARTNER, false);

        // ----- Bind UI -----
        View title = v.findViewById(R.id.bsCompanyName);
        View phoneTv = v.findViewById(R.id.bsPhone);
        View emailTv = v.findViewById(R.id.bsEmail);
        View webTv = v.findViewById(R.id.bsWeb);

        View callBtn = v.findViewById(R.id.bsCallBtn);
        View mailBtn = v.findViewById(R.id.bsMailBtn);
        View webBtn = v.findViewById(R.id.bsWebBtn);
        View sendDetailsBtn = v.findViewById(R.id.bsLeaveDetailsBtn);

        if (title instanceof android.widget.TextView) ((android.widget.TextView) title).setText(name);
        if (phoneTv instanceof android.widget.TextView) ((android.widget.TextView) phoneTv).setText(phone);
        if (emailTv instanceof android.widget.TextView) ((android.widget.TextView) emailTv).setText(email);
        if (webTv instanceof android.widget.TextView) ((android.widget.TextView) webTv).setText(web);

        // ----- UI actions (Intents) -----
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

        // Hide buttons if info missing
        if (phone == null || phone.trim().isEmpty()) callBtn.setVisibility(View.GONE);
        if (email == null || email.trim().isEmpty()) mailBtn.setVisibility(View.GONE);
        if (web == null || web.trim().isEmpty()) webBtn.setVisibility(View.GONE);

        // Partner-only: send details
        if (sendDetailsBtn != null) {
            sendDetailsBtn.setVisibility(isPartner ? View.VISIBLE : View.GONE);

            sendDetailsBtn.setOnClickListener(btn -> {
                // ✅ כאן נשלח: hp + docId
                vm.sendMyDetails(companyIdHp, companyDocId, name);
            });
        }

        return v;
    }
}
