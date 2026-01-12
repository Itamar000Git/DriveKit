package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.R;
import com.example.drive_kit.View.Adapter.CarsAdapter;
import com.example.drive_kit.ViewModel.CarViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class MyCarsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cars);

        RecyclerView rv = findViewById(R.id.carsRecycler);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        CarsAdapter adapter = new CarsAdapter(car -> {
            Intent i = new Intent(MyCarsActivity.this, CarDetailsActivity.class);
            i.putExtra("carId", car.getCarNum());
            i.putExtra("manufacturer", car.getManufacturer());
            i.putExtra("model", car.getModel());
            i.putExtra("year", car.getYear());
            i.putExtra("color", car.getCarColor());
            i.putExtra("plate", car.getPlate());
            i.putExtra("nickname", car.getNickname());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getUid();
        CarViewModel vm = new ViewModelProvider(this).get(CarViewModel.class);

        if (uid != null) {
            vm.loadCars(uid);
            vm.getCars().observe(this, adapter::submit);
        }
        TextView empty = findViewById(R.id.emptyCarsText);

        vm.getCars().observe(this, list -> {
            adapter.submit(list);
            empty.setVisibility((list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);
        });

    }
}
