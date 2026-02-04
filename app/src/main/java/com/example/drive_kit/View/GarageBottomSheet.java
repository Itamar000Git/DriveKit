//package com.example.drive_kit.View;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.example.drive_kit.R;
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
//import com.google.android.material.button.MaterialButton;
//
//import java.util.Locale;
//
//public class GarageBottomSheet extends BottomSheetDialogFragment {
//
//    private static final String ARG_NAME = "arg_name";
//    private static final String ARG_ADDRESS = "arg_address";
//    private static final String ARG_LAT = "arg_lat";
//    private static final String ARG_LNG = "arg_lng";
//    private static final String ARG_DISTANCE = "arg_distance";
//    private static final String ARG_PHONE = "arg_phone";
//    private static final String ARG_WEBSITE = "arg_website";
//
//    public static GarageBottomSheet newInstance(activity_nearby_garages.GarageItem item) {
//        GarageBottomSheet bs = new GarageBottomSheet();
//        Bundle b = new Bundle();
//        b.putString(ARG_NAME, item.name);
//        b.putString(ARG_ADDRESS, item.address);
//        b.putDouble(ARG_LAT, item.latLng.latitude);
//        b.putDouble(ARG_LNG, item.latLng.longitude);
//        b.putDouble(ARG_DISTANCE, item.distanceMeters);
//        b.putString(ARG_PHONE, item.phone);
//        b.putString(ARG_WEBSITE, item.website);
//        bs.setArguments(b);
//        return bs;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.item_garage, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(v, savedInstanceState);
//
//        TextView tvName = v.findViewById(R.id.tvGarageName);
//        TextView tvAddress = v.findViewById(R.id.tvGarageAddress);
//        TextView tvDistance = v.findViewById(R.id.tvDistance);
//
//        MaterialButton btnNavigate = v.findViewById(R.id.btnNavigate);
//        MaterialButton btnCall = v.findViewById(R.id.btnCall);
//        MaterialButton btnWebsite = v.findViewById(R.id.btnWebsite);
//
//        Bundle a = getArguments();
//        if (a == null) {
//            dismiss();
//            return;
//        }
//
//        String name = safe(a.getString(ARG_NAME));
//        String address = safe(a.getString(ARG_ADDRESS));
//        double lat = a.getDouble(ARG_LAT, 0);
//        double lng = a.getDouble(ARG_LNG, 0);
//        double distM = a.getDouble(ARG_DISTANCE, 0);
//        String phone = a.getString(ARG_PHONE);
//        String website = a.getString(ARG_WEBSITE);
//
//        tvName.setText(name);
//        tvAddress.setText(address);
//
//        if (distM > 0) {
//            tvDistance.setText(String.format(Locale.getDefault(), "%.1f ק״מ", distM / 1000.0));
//        } else {
//            tvDistance.setText("-");
//        }
//
//        // Navigate
//        btnNavigate.setOnClickListener(x -> openNav(lat, lng));
//
//        // Call (אם יש)
//        if (phone == null || phone.trim().isEmpty()) {
//            btnCall.setEnabled(false);
//            btnCall.setAlpha(0.5f);
//        } else {
//            btnCall.setEnabled(true);
//            btnCall.setAlpha(1f);
//            btnCall.setOnClickListener(x -> {
//                try {
//                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
//                    startActivity(i);
//                } catch (Exception e) {
//                    Toast.makeText(requireContext(), "לא ניתן לפתוח חיוג", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//        // Website (אם יש)
//        if (website == null || website.trim().isEmpty()) {
//            btnWebsite.setEnabled(false);
//            btnWebsite.setAlpha(0.5f);
//        } else {
//            btnWebsite.setEnabled(true);
//            btnWebsite.setAlpha(1f);
//            btnWebsite.setOnClickListener(x -> {
//                try {
//                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
//                    startActivity(i);
//                } catch (Exception e) {
//                    Toast.makeText(requireContext(), "לא ניתן לפתוח אתר", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
//
//    private void openNav(double lat, double lng) {
//        try {
//            Uri uri = Uri.parse("google.navigation:q=" + lat + "," + lng + "&mode=d");
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            intent.setPackage("com.google.android.apps.maps");
//            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
//                startActivity(intent);
//            } else {
//                Uri fallback = Uri.parse("geo:" + lat + "," + lng);
//                startActivity(new Intent(Intent.ACTION_VIEW, fallback));
//            }
//        } catch (Exception e) {
//            Toast.makeText(requireContext(), "לא ניתן לפתוח ניווט", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private String safe(String s) {
//        if (s == null || s.trim().isEmpty()) return "-";
//        return s;
//    }
//}
