package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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

public class TicketManagerFragment extends AppCompatActivity  {
    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private List<Ticket> ticketList = new ArrayList<>();
    private Spinner ticketTypeSpinner;
    private Button btnHistory;
    private DatabaseReference ticketsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ticket_manager);

        recyclerView = findViewById(R.id.ticketRecyclerView);
        ticketTypeSpinner = findViewById(R.id.ticketTypeSpinner);
        btnHistory = findViewById(R.id.btnHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(ticketList, this);
        recyclerView.setAdapter(adapter);

        ticketsRef = FirebaseDatabase.getInstance().getReference("Tickets");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Tất cả", "single", "monthly", "yearly"});
        ticketTypeSpinner.setAdapter(spinnerAdapter);

        ticketTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadTickets(ticketTypeSpinner.getSelectedItem().toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, TicketHistoryActivity.class)));
    }

    private void loadTickets(String filterType) {
        ticketsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Ticket ticket = data.getValue(Ticket.class);
                    if (ticket != null) {
                        if (filterType.equals("Tất cả") || ticket.ticketType.equals(filterType)) {
                            ticketList.add(ticket);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketManagerFragment.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
