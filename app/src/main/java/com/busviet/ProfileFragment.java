package com.busviet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.*;

public class ProfileFragment extends Fragment {

    private DatabaseReference usersRef;
    private String username;


    public ProfileFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvInfo = view.findViewById(R.id.tvUserInfo);

        // Lấy username từ HomeActivity
        if (getActivity() != null && getActivity().getIntent() != null) {
            username = getActivity().getIntent().getStringExtra("username");
        }

        // Truy cập Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    String info = "👤 Tài khoản: " + username +
                            "\n🔒 Mật khẩu: " + user.password +
                            "\n📍 Địa chỉ: " + user.contact +
                            "\n📞 Số điện thoại: " + user.phone;
                    tvInfo.setText(info);
                } else {
                    tvInfo.setText("❌ Không tìm thấy thông tin người dùng.");
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvInfo.setText("⚠️ Lỗi khi tải dữ liệu: " + error.getMessage());
            }

        });

        Button btnEdit = view.findViewById(R.id.btnEditUser);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
        return view;
    }
}
