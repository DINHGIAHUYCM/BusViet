package com.busviet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateRouteFragment extends Fragment {

    private EditText etRouteCode, etStartPoint, etEndPoint;
    private RadioGroup rgPrice;
    private RadioButton rbPrice120, rbPrice250;
    private Switch switchActive;
    private Button btnCreate;

    private DatabaseReference busRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_route, container, false);

        etRouteCode = view.findViewById(R.id.etRouteCode);
        etStartPoint = view.findViewById(R.id.etStartPoint);
        etEndPoint = view.findViewById(R.id.etEndPoint);
        rgPrice = view.findViewById(R.id.rgPrice);
        rbPrice120 = view.findViewById(R.id.rbPrice120);
        rbPrice250 = view.findViewById(R.id.rbPrice250);
        switchActive = view.findViewById(R.id.switchActive);
        btnCreate = view.findViewById(R.id.btnCreate);

        busRef = FirebaseDatabase.getInstance().getReference("bus");

        btnCreate.setOnClickListener(v -> createBusRoute());

        return view;
    }

    private void createBusRoute() {
        String routeCode = etRouteCode.getText().toString().trim();
        String startPoint = etStartPoint.getText().toString().trim();
        String endPoint = etEndPoint.getText().toString().trim();
        boolean active = switchActive.isChecked();

        if (TextUtils.isEmpty(routeCode) || TextUtils.isEmpty(startPoint) || TextUtils.isEmpty(endPoint)) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin tuyến", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = 0;
        if (rbPrice120.isChecked()) {
            price = 120000;
        } else if (rbPrice250.isChecked()) {
            price = 250000;
        } else {
            Toast.makeText(getContext(), "Vui lòng chọn mệnh giá vé", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = busRef.push().getKey();
        Bus bus = new Bus(key, routeCode, startPoint, endPoint, price, active);

        busRef.child(key).setValue(bus).addOnSuccessListener(unused ->
                Toast.makeText(getContext(), "✅ Đã tạo tuyến thành công", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(getContext(), "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }
}