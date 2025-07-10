package com.busviet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TicketHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private List<TicketWithBus> ticketWithBusList = new ArrayList<>();
    private List<Bus> busList = new ArrayList<>();
    private DatabaseReference ticketsRef, busRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_history); // Sử dụng layout activity thay vì fragment

        // Khởi tạo view
        recyclerView = findViewById(R.id.ticketRecyclerView);

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(ticketWithBusList, this);
        recyclerView.setAdapter(adapter);

        // Khởi tạo Firebase references
        ticketsRef = FirebaseDatabase.getInstance().getReference("purchases");
        busRef = FirebaseDatabase.getInstance().getReference("bus");

        // Load dữ liệu ban đầu
        loadBusData(() -> loadTickets());

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadBusData(Runnable onComplete) {
        busRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                busList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Bus bus = data.getValue(Bus.class);
                    if (bus != null) {
                        busList.add(bus);
                    }
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketHistoryActivity.this, "Lỗi tải dữ liệu tuyến xe", Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void loadTickets() {
        ticketsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketWithBusList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Ticket ticket = data.getValue(Ticket.class);
                    if (ticket != null && ticket.ticketCount <= 0) {
                        Bus matchingBus = findBusByRouteCode(ticket.routeCode);
                        ticketWithBusList.add(new TicketWithBus(ticket, matchingBus));
                        Log.d("TicketDebug", "Loaded ticket: " + ticket.ticketType + " with bus: " +
                                (matchingBus != null ? matchingBus.routeCode : "null"));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketHistoryActivity.this, "Lỗi tải dữ liệu vé", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bus findBusByRouteCode(String routeCode) {
        if (routeCode == null) return null;

        for (Bus bus : busList) {
            if (bus.routeCode != null && bus.routeCode.equals(routeCode)) {
                return bus;
            }
        }
        return null;
    }

}