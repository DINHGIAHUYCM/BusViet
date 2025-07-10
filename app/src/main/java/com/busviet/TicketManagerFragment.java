package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TicketManagerFragment extends Fragment {

    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private List<TicketWithBus> ticketWithBusList = new ArrayList<>();
    private List<Bus> busList = new ArrayList<>();
    private Spinner ticketTypeSpinner;
    private Button btnHistory;
    private DatabaseReference ticketsRef, busRef;

    public TicketManagerFragment() {
        // Constructor rỗng
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ticket_manager, container, false);

        // Khởi tạo view
        recyclerView = view.findViewById(R.id.ticketRecyclerView);
        ticketTypeSpinner = view.findViewById(R.id.ticketTypeSpinner);
        btnHistory = view.findViewById(R.id.btnHistory);

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TicketAdapter(ticketWithBusList, getContext());
        recyclerView.setAdapter(adapter);

        // Khởi tạo Firebase references
        ticketsRef = FirebaseDatabase.getInstance().getReference("purchases");
        busRef = FirebaseDatabase.getInstance().getReference("bus");

        // Thiết lập Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Tất cả", "single", "monthly", "quarterly", "yearly"});
        ticketTypeSpinner.setAdapter(spinnerAdapter);

        ticketTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadBusData(() -> loadTickets(ticketTypeSpinner.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Xử lý sự kiện nút lịch sử
        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), TicketHistoryActivity.class))
        );

        // Load dữ liệu ban đầu
        loadBusData(() -> loadTickets("Tất cả"));

        return view;
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
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu tuyến xe", Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void loadTickets(String filterType) {
        ticketsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketWithBusList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Ticket ticket = data.getValue(Ticket.class);
                    if (ticket != null && (filterType.equals("Tất cả") && ticket.ticketCount > 0 || ticket.ticketType.equalsIgnoreCase(filterType))) {
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
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu vé", Toast.LENGTH_SHORT).show();
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