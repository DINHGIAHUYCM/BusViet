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
    private List<Ticket> ticketList;
    private Context context;

    public TicketAdapter(List<Ticket> ticketList, Context context) {
        this.ticketList = ticketList;
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
        Ticket ticket = ticketList.get(position);
        holder.txtTicketType.setText("Loại vé: " + ticket.ticketType);
        holder.txtRouteCode.setText("Tuyến: " + ticket.routeCode);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.txtPurchaseDate.setText("Ngày mua: " + sdf.format(new Date(ticket.purchaseDate)));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TicketDetailActivity.class);
            // Truyền thêm dữ liệu nếu cần
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView txtTicketType, txtRouteCode, txtPurchaseDate;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTicketType = itemView.findViewById(R.id.txtTicketType);
            txtRouteCode = itemView.findViewById(R.id.txtRouteCode);
            txtPurchaseDate = itemView.findViewById(R.id.txtPurchaseDate);
        }
    }
}
