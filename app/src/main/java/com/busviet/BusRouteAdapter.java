package com.busviet;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class BusRouteAdapter extends RecyclerView.Adapter<BusRouteAdapter.BusRouteViewHolder> {

    private List<BusRoute> busRoutes;
    private OnRouteClickListener listener;

    public interface OnRouteClickListener {
        void onRouteClick(BusRoute route, List<LatLng> polyline);
    }

    public BusRouteAdapter(List<BusRoute> busRoutes, OnRouteClickListener listener) {
        this.busRoutes = busRoutes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BusRouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus_route, parent, false);
        return new BusRouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusRouteViewHolder holder, int position) {
        BusRoute route = busRoutes.get(position);
        holder.bind(route);
    }

    @Override
    public int getItemCount() {
        return busRoutes.size();
    }

    public class BusRouteViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBusNumber;
        private TextView tvDepartureStop;
        private TextView tvArrivalStop;
        private TextView tvNumStops;

        public BusRouteViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // ✅ Kiểm tra findViewById có trả về null không
            tvBusNumber = itemView.findViewById(R.id.tvBusNumber);
            tvDepartureStop = itemView.findViewById(R.id.tvDepartureStop);
            tvArrivalStop = itemView.findViewById(R.id.tvArrivalStop);
            tvNumStops = itemView.findViewById(R.id.tvNumStops);
            
            // ✅ Log để debug nếu View bị null
            if (tvBusNumber == null) {
                Log.e("BusRouteAdapter", "tvBusNumber is null - check R.id.tvBusNumber in item_bus_route.xml");
            }
            if (tvDepartureStop == null) {
                Log.e("BusRouteAdapter", "tvDepartureStop is null - check R.id.tvDepartureStop in item_bus_route.xml");
            }
            if (tvArrivalStop == null) {
                Log.e("BusRouteAdapter", "tvArrivalStop is null - check R.id.tvArrivalStop in item_bus_route.xml");
            }
            if (tvNumStops == null) {
                Log.e("BusRouteAdapter", "tvNumStops is null - check R.id.tvNumStops in item_bus_route.xml");
            }
        }

        public void bind(BusRoute route) {
            // ✅ Kiểm tra null trước khi set text
            if (tvBusNumber != null) {
                tvBusNumber.setText("Bus: " + (route.getBusNumber() != null ? route.getBusNumber() : "N/A"));
            }
            
            if (tvDepartureStop != null) {
                tvDepartureStop.setText("Từ: " + (route.getDepartureStop() != null ? route.getDepartureStop() : "N/A"));
            }
            
            if (tvArrivalStop != null) {
                tvArrivalStop.setText("Đến: " + (route.getArrivalStop() != null ? route.getArrivalStop() : "N/A"));
            }
            
            if (tvNumStops != null) {
                tvNumStops.setText("Số trạm: " + route.getNumStops());
            }

            // ✅ Set click listener an toàn
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRouteClick(route, route.getPolyline());
                }
            });
        }
    }
}
