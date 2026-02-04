package com.example.drive_kit.View;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;
import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;
import com.example.drive_kit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class InsuranceActivity extends BaseLoggedInActivity {

    private RecyclerView recycler;
    private InsuranceInquiryRepository inquiryRepo;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.insurance_activity);

        recycler = findViewById(R.id.insuranceRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        inquiryRepo = new InsuranceInquiryRepository();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = (user == null) ? null : user.getUid();

        InsuranceCompaniesRepository repo = new InsuranceCompaniesRepository();

        repo.loadCarCompanies(new InsuranceCompaniesRepository.Callback() {
            @Override
            public void onResult(List<com.example.drive_kit.View.InsuranceCompany> companies) {

                InsuranceListAdapter adapter = new InsuranceListAdapter(companies, company -> {

                    InsuranceBottomSheet sheet = InsuranceBottomSheet.newInstance(
                            company.getId(),
                            company.getName(),
                            company.getPhone(),
                            company.getEmail(),
                            company.getWebsite(),
                            company.isPartner()
                    );


                    sheet.show(getSupportFragmentManager(), "insurance_sheet");

                    if (company.isPartner()) {
                        inquiryRepo.logInquiry(uid, company.getId(), company.getName());
                    }
                });

                recycler.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(InsuranceActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                Toast.makeText(InsuranceActivity.this,
                        "Firestore error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }



        });
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.insurance_activity;
    }
}
