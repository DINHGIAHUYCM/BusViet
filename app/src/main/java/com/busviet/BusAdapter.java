package com.busviet;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Button;
import android.widget.LinearLayout;


import java.util.List;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.BusViewHolder> {

    private List<Bus> busList;
    private String role;
    private OnBusActionListener listener;

    public interface OnBusActionListener {
        void onEdit(Bus bus);
        void onDelete(Bus bus);
        void onBuy(Bus bus);
    }

    public BusAdapter(List<Bus> busList, String role, OnBusActionListener listener) {
        this.busList = busList;
        this.role = role;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus, parent, false);
        return new BusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusViewHolder holder, int position) {
        Bus bus = busList.get(position);
        holder.tvRouteCode.setText("Mã tuyến: " + bus.routeCode);
        holder.tvRoute.setText(bus.startPoint + " ➜ " + bus.endPoint);
        holder.tvPrice.setText("Giá vé: " + bus.ticketPrice + "đ");
        holder.tvStatus.setText("Trạng thái: " + (bus.active ? "Hoạt động" : "Ngừng"));

        if ("Admin".equals(role)) {
            holder.adminActions.setVisibility(View.VISIBLE);
            holder.btnBuy.setVisibility(View.GONE);

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(bus));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(bus));
        } else {
            holder.adminActions.setVisibility(View.GONE);
            holder.btnBuy.setVisibility(View.VISIBLE);
            holder.btnBuy.setOnClickListener(v -> listener.onBuy(bus));
        }
    }

    @Override
    public int getItemCount() {
        return busList.size();
    }

    public static class BusViewHolder extends RecyclerView.ViewHolder {
        TextView tvRouteCode, tvRoute, tvPrice, tvStatus;
        Button btnEdit, btnDelete, btnBuy;
        LinearLayout adminActions;

        public BusViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRouteCode = itemView.findViewById(R.id.tvRouteCode);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnBuy = itemView.findViewById(R.id.btnBuy);
            adminActions = itemView.findViewById(R.id.adminActions);
        }
    }
}
