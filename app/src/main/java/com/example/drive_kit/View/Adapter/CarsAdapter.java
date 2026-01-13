package com.example.drive_kit.View.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.Model.Car;
import com.example.drive_kit.R;

import java.util.ArrayList;
import java.util.List;
//
//public class CarsAdapter extends RecyclerView.Adapter<CarsAdapter.VH> {
////
////    public interface OnCarClick {
////        void onClick(Car car);
////    }
////
////    private final OnCarClick listener;
////    private List<Car> data = new ArrayList<>();
////
////    public CarsAdapter(OnCarClick listener) {
////        this.listener = listener;
////    }
////
////    public void submit(List<Car> list) {
////        data = (list == null) ? new ArrayList<>() : list;
////        notifyDataSetChanged();
////    }
////
////    @NonNull
////    @Override
////    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
////        View v = LayoutInflater.from(parent.getContext())
////                .inflate(R.layout.item_car_circle, parent, false);
////        return new VH(v);
////    }
////
////    @Override
////    public void onBindViewHolder(@NonNull VH h, int position) {
////        Car car = data.get(position);
////
////        String label = car.getNickname();
////        if (label == null || label.trim().isEmpty()) label = car.getCarNum();
////        if (label == null || label.trim().isEmpty()) label = "רכב";
////
////        h.text.setText(label);
////
////        h.itemView.setOnClickListener(v -> listener.onClick(car));
////    }
////
////    @Override
////    public int getItemCount() {
////        return data.size();
////    }
////
////    static class VH extends RecyclerView.ViewHolder {
////        TextView text;
////        VH(@NonNull View itemView) {
////            super(itemView);
////            text = itemView.findViewById(R.id.carCircleText);
////        }
////    }
//}
