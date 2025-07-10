package com.busviet;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<TicketWithBus> ticketWithBusList;
    private Context context;

    public TicketAdapter(List<TicketWithBus> ticketWithBusList, Context context) {
        this.ticketWithBusList = ticketWithBusList;
        this.context = context;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketWithBus item = ticketWithBusList.get(position);
        Ticket ticket = item.ticket;
        Bus bus = item.bus;

        // Xử lý null safety
        if (ticket == null) return;

        holder.txtTicketType.setText("Loại vé: " + ticket.ticketType);
        holder.txtRouteCode.setText("Tuyến: " + ticket.routeCode);
        holder.txtIsValid.setText("Còn hiệu lực: " + (ticket.isValid ? "Có" : "Không"));
        holder.txtValidInCurrentMonth.setText("Còn hiệu lực tháng này: " + (ticket.validInCurrentMonth ? "Có" : "Không"));
        holder.txtTicketCount.setText("Số lượng vé còn lại: " + ticket.ticketCount);

        // Xử lý thông tin Bus (có thể null)
        if (bus != null) {
            holder.txtRoute.setText(bus.startPoint + " ➜ " + bus.endPoint);
            holder.txtStatus.setText("Trạng thái: " + (bus.active ? "Hoạt động" : "Ngừng"));
        } else {
            holder.txtRoute.setText("Không có thông tin tuyến xe");
            holder.txtStatus.setText("Trạng thái: Không xác định");
        }

        // Xử lý ngày mua
        if (ticket.purchaseDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.txtPurchaseDate.setText("Ngày mua: " + sdf.format(new Date(ticket.purchaseDate)));
        } else {
            holder.txtPurchaseDate.setText("Ngày mua: Không xác định");
        }


        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TicketDetailActivity.class);
            intent.putExtra("ticket", ticket);
            intent.putExtra("bus", bus);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ticketWithBusList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView txtTicketType, txtRoute, txtTicketCount, txtRouteCode, txtPurchaseDate, txtStatus, txtIsValid, txtValidInCurrentMonth;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTicketType = itemView.findViewById(R.id.txtTicketType);
            txtRouteCode = itemView.findViewById(R.id.txtRouteCode);
            txtPurchaseDate = itemView.findViewById(R.id.txtPurchaseDate);
            txtRoute = itemView.findViewById(R.id.txtRoute);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtIsValid = itemView.findViewById(R.id.txtIsValid);
            txtValidInCurrentMonth = itemView.findViewById(R.id.txtValidInCurrentMonth);
            txtTicketCount = itemView.findViewById(R.id.txtTicketCount);
        }
    }

    // Cập nhật dữ liệu
    public void updateData(List<TicketWithBus> newList) {
        ticketWithBusList.clear();
        ticketWithBusList.addAll(newList);
        notifyDataSetChanged();
    }
}