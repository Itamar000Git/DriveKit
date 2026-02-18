package com.example.drive_kit.View;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.R;
import com.example.drive_kit.View.Adapter.InsuranceListAdapter;
import com.example.drive_kit.ViewModel.DriverInsuranceListViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Driver_InsuranceActivity extends BaseLoggedInActivity {

    private RecyclerView recycler;
    private DriverInsuranceListViewModel vm;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recycler = findViewById(R.id.insuranceRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = (user == null) ? null : user.getUid();

        vm = new ViewModelProvider(this).get(DriverInsuranceListViewModel.class);

        vm.getCompanies().observe(this, companies -> {
            recycler.setAdapter(new InsuranceListAdapter(companies, company -> {

                // ✅ הכי חשוב: hp + docId
                Driver_InsuranceBottomSheet sheet = Driver_InsuranceBottomSheet.newInstance(
                        company.getHp(),       // h_p (515761625)
                        company.getDocId(),    // docId (libra)
                        company.getName(),
                        company.getPhone(),
                        company.getEmail(),
                        company.getWebsite(),
                        company.isPartner()
                );
                sheet.show(getSupportFragmentManager(), "insurance_sheet");

                // business logic
                vm.onCompanyClicked(uid, company);
            }));
        });

        vm.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, "Firestore error: " + msg, Toast.LENGTH_LONG).show();
            }
        });

        vm.loadCompanies();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.insurance_activity;
    }
}
