package com.busviet;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.database.*;

import java.util.*;

public class BusRoutesFragment extends Fragment {

    private RecyclerView rvBusList;
    private List<Bus> busList = new ArrayList<>();
    private String role = "Customer"; // mặc định

    public BusRoutesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bus_routes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            role = getArguments().getString("role", "Customer");
        }

        rvBusList = view.findViewById(R.id.rvBusList);
        rvBusList.setLayoutManager(new LinearLayoutManager(getContext()));

        BusAdapter adapter = new BusAdapter(busList, role, new BusAdapter.OnBusActionListener() {
            @Override
            public void onEdit(Bus bus) {
                EditBusDialogFragment dialog = EditBusDialogFragment.newInstance(bus);
                dialog.show(getChildFragmentManager(), "EditDialog");
            }

            @Override
            public void onDelete(Bus bus) {
                FirebaseDatabase.getInstance().getReference("bus")
                        .child(bus.id)
                        .removeValue()
                        .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Đã xóa tuyến!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onBuy(Bus bus) {
                Toast.makeText(getContext(), "🚍 Mua vé tuyến: " + bus.routeCode, Toast.LENGTH_SHORT).show();
            }
        });

        rvBusList.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference("bus")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        busList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Bus bus = snap.getValue(Bus.class);
                            if (bus != null) {
                                busList.add(bus);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
