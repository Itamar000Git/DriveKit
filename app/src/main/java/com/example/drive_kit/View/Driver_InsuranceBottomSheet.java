//package com.example.drive_kit.View;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;
//import com.example.drive_kit.Model.Driver;
//import com.example.drive_kit.R;
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class Driver_InsuranceBottomSheet extends BottomSheetDialogFragment {
//
//    private static final String ARG_ID = "id";
//    private static final String ARG_NAME = "name";
//    private static final String ARG_PHONE = "phone";
//    private static final String ARG_EMAIL = "email";
//    private static final String ARG_WEB = "web";
//    private static final String ARG_IS_PARTNER = "isPartner";
//
//    public static Driver_InsuranceBottomSheet newInstance(
//            String id,
//            String name,
//            String phone,
//            String email,
//            String web,
//            boolean isPartner
//    ) {
//        Driver_InsuranceBottomSheet f = new Driver_InsuranceBottomSheet();
//        Bundle b = new Bundle();
//        b.putString(ARG_ID, id); // companyId
//        b.putString(ARG_NAME, name);
//        b.putString(ARG_PHONE, phone);
//        b.putString(ARG_EMAIL, email);
//        b.putString(ARG_WEB, web);
//        b.putBoolean(ARG_IS_PARTNER, isPartner);
//        f.setArguments(b);
//        return f;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.bottomsheet_insurance_company, container, false);
//
//        Bundle args = getArguments();
//        String companyId = args != null ? args.getString(ARG_ID, "") : "";
//        String name = args != null ? args.getString(ARG_NAME, "") : "";
//        String phone = args != null ? args.getString(ARG_PHONE, "") : "";
//        String email = args != null ? args.getString(ARG_EMAIL, "") : "";
//        String web = args != null ? args.getString(ARG_WEB, "") : "";
//        boolean isPartner = args != null && args.getBoolean(ARG_IS_PARTNER, false);
//
//        View title = v.findViewById(R.id.bsCompanyName);
//        View phoneTv = v.findViewById(R.id.bsPhone);
//        View emailTv = v.findViewById(R.id.bsEmail);
//        View webTv = v.findViewById(R.id.bsWeb);
//
//        View callBtn = v.findViewById(R.id.bsCallBtn);
//        View mailBtn = v.findViewById(R.id.bsMailBtn);
//        View webBtn = v.findViewById(R.id.bsWebBtn);
//        View sendDetailsBtn = v.findViewById(R.id.bsLeaveDetailsBtn);
//
//        if (title instanceof android.widget.TextView) ((android.widget.TextView) title).setText(name);
//        if (phoneTv instanceof android.widget.TextView) ((android.widget.TextView) phoneTv).setText(phone);
//        if (emailTv instanceof android.widget.TextView) ((android.widget.TextView) emailTv).setText(email);
//        if (webTv instanceof android.widget.TextView) ((android.widget.TextView) webTv).setText(web);
//
//        callBtn.setOnClickListener(btn -> {
//            if (phone == null || phone.trim().isEmpty()) return;
//            String encoded = Uri.encode(phone);
//            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + encoded)));
//        });
//
//        mailBtn.setOnClickListener(btn -> {
//            if (email == null || email.trim().isEmpty()) return;
//            Intent i = new Intent(Intent.ACTION_SENDTO);
//            i.setData(Uri.parse("mailto:" + email));
//            i.putExtra(Intent.EXTRA_SUBJECT, "פנייה דרך DriveKit");
//            startActivity(i);
//        });
//
//        webBtn.setOnClickListener(btn -> {
//            if (web == null || web.trim().isEmpty()) return;
//            String url = web.startsWith("http") ? web : ("https://" + web);
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//        });
//
//        if (phone == null || phone.trim().isEmpty()) callBtn.setVisibility(View.GONE);
//        if (email == null || email.trim().isEmpty()) mailBtn.setVisibility(View.GONE);
//        if (web == null || web.trim().isEmpty()) webBtn.setVisibility(View.GONE);
//
//        // Show "Send my details" only for partner companies
//        if (sendDetailsBtn != null) {
//            sendDetailsBtn.setVisibility(isPartner ? View.VISIBLE : View.GONE);
//
//            sendDetailsBtn.setOnClickListener(btn -> {
//                String userId = FirebaseAuth.getInstance().getUid();
//                if (userId == null || userId.trim().isEmpty()) {
//                    Toast.makeText(requireContext(), "לא נמצא משתמש מחובר", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                if (companyId == null || companyId.trim().isEmpty()) {
//                    Toast.makeText(requireContext(), "לא נמצא מזהה חברה", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                // Pull latest driver data directly from Firestore (stable across screens)
//                FirebaseFirestore.getInstance()
//                        .collection("drivers")
//                        .document(userId)
//                        .get()
//                        .addOnSuccessListener(doc -> {
//                            Driver d = doc.toObject(Driver.class);
//
//                            String driverName = "";
//                            String driverPhone = "";
//                            String driverEmail = "";
//                            String carNumber = "";
//                            String carModel = "";
//
//                            if (d != null) {
//                                String first = d.getFirstName() == null ? "" : d.getFirstName().trim();
//                                String last = d.getLastName() == null ? "" : d.getLastName().trim();
//                                driverName = (first + " " + last).trim();
//
//                                driverPhone = d.getPhone() == null ? "" : d.getPhone().trim();
//                                driverEmail = d.getEmail() == null ? "" : d.getEmail().trim();
//                                carNumber = (d.getCar() != null && d.getCar().getCarNumber() != null)
//                                        ? d.getCar().getCarNumber().trim() : "";
//                                carModel = (d.getCar() != null && d.getCar().getCarModel() != null)
//                                        ? d.getCar().getCarModel().name() : "";
//                            }
//
//                            InsuranceInquiryRepository repo = new InsuranceInquiryRepository();
//                            repo.logInquiry(
//                                    userId,
//                                    companyId,
//                                    name, // companyName
//                                    driverName,
//                                    driverPhone,
//                                    driverEmail,
//                                    carNumber,
//                                    carModel,
//                                    "הנהג ביקש שיחזרו אליו דרך DriveKit",
//                                    new InsuranceInquiryRepository.InquiryCallback() {
//                                        @Override
//                                        public void onSuccess() {
//                                            Toast.makeText(requireContext(), "הפרטים נשלחו לחברת הביטוח", Toast.LENGTH_SHORT).show();
//                                            dismiss();
//                                        }
//
//                                        @Override
//                                        public void onError(Exception e) {
//                                            String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "";
//                                            Toast.makeText(requireContext(), "שליחה נכשלה: " + msg, Toast.LENGTH_LONG).show();
//                                        }
//                                    }
//                            );
//                        })
//                        .addOnFailureListener(e -> {
//                            String msg = (e.getMessage() != null) ? e.getMessage() : "";
//                            Toast.makeText(requireContext(), "שגיאה בטעינת פרטי נהג: " + msg, Toast.LENGTH_LONG).show();
//                        });
//            });
//        }
//
//        return v;
//    }
//}
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

/**
 * Driver_InsuranceBottomSheet
 *
 * BottomSheet UI that shows details for a single insurance company.
 *
 * What stays in the UI (here):
 * - Binding views (TextViews / Buttons)
 * - Intents: call / email / website
 * - Showing Toast messages
 * - Dismissing the sheet on success
 *
 * What moves to MVVM (ViewModel):
 * - Fetching the latest Driver data from Firestore
 * - Sending an inquiry using InsuranceInquiryRepository
 */
public class Driver_InsuranceBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_WEB = "web";
    private static final String ARG_IS_PARTNER = "isPartner";

    private DriverInsuranceSheetViewModel vm;

    public static Driver_InsuranceBottomSheet newInstance(
            String id,
            String name,
            String phone,
            String email,
            String web,
            boolean isPartner
    ) {
        Driver_InsuranceBottomSheet f = new Driver_InsuranceBottomSheet();
        Bundle b = new Bundle();
        b.putString(ARG_ID, id); // companyId
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

        // ViewModel for this sheet instance
        vm = new ViewModelProvider(this).get(DriverInsuranceSheetViewModel.class);

        // ----- Observe ViewModel state (UI reactions) -----

        vm.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        vm.getSendSuccess().observe(getViewLifecycleOwner(), success -> {
            // Same behavior as before: show success toast and close the sheet
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "הפרטים נשלחו לחברת הביטוח", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        // NOTE: sending LiveData exists in the VM.
        // If you have a ProgressBar in the sheet layout, you can bind it here.
        // For now we do nothing, so behavior stays the same.
        // vm.getSending().observe(getViewLifecycleOwner(), sending -> { ... });

        // ----- Read args -----

        Bundle args = getArguments();
        String companyId = args != null ? args.getString(ARG_ID, "") : "";
        String name = args != null ? args.getString(ARG_NAME, "") : "";
        String phone = args != null ? args.getString(ARG_PHONE, "") : "";
        String email = args != null ? args.getString(ARG_EMAIL, "") : "";
        String web = args != null ? args.getString(ARG_WEB, "") : "";
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

        // Hide buttons if info is missing (same behavior)
        if (phone == null || phone.trim().isEmpty()) callBtn.setVisibility(View.GONE);
        if (email == null || email.trim().isEmpty()) mailBtn.setVisibility(View.GONE);
        if (web == null || web.trim().isEmpty()) webBtn.setVisibility(View.GONE);

        // ----- Partner-only action: "Send my details" -----

        if (sendDetailsBtn != null) {
            sendDetailsBtn.setVisibility(isPartner ? View.VISIBLE : View.GONE);

            sendDetailsBtn.setOnClickListener(btn -> {
                // Delegates the exact same flow to the ViewModel:
                // - validate user
                // - load latest driver from Firestore
                // - log inquiry
                vm.sendMyDetails(companyId, name);
            });
        }

        return v;
    }
}
