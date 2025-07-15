package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.*;

public class ProfileFragment extends Fragment {

    private DatabaseReference usersRef;
    private String username, role;
    private TextView tvInfo;
    private Button btnLogout;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvInfo = view.findViewById(R.id.tvUserInfo);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Lấy username và role từ HomeActivity
        if (getActivity() != null && getActivity().getIntent() != null) {
            username = getActivity().getIntent().getStringExtra("username");
            role = getActivity().getIntent().getStringExtra("role");
        }

        // Truy cập Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUserInfo();
        setupLogoutButton();

        Button btnEdit = view.findViewById(R.id.btnEditUser);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
        return view;
    }

    private void loadUserInfo() {
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    String info = "👤 Tài khoản: " + username +
                            "\n🔑 Vai trò: " + (role != null ? role : "Customer") +
                            "\n📧 Địa chỉ: " + (user.contact != null ? user.contact : "Chưa cập nhật") +
                            "\n📞 Số điện thoại: " + (user.phone != null ? user.phone : "Chưa cập nhật");
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
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("🚪 Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    private void logout() {
        try {
            // Hiển thị thông báo
            Toast.makeText(getContext(), "👋 Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            
            // Quay về MainActivity
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            
            // Đóng activity hiện tại
            if (getActivity() != null) {
                getActivity().finish();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "❌ Lỗi đăng xuất: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
