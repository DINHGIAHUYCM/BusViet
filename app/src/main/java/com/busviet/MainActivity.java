package com.busviet;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class MainActivity extends AppCompatActivity {

    EditText etUsername, etPassword, etContact, etPhone;
    Button btnLogin;
    TextView tvStatus;

    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etContact = findViewById(R.id.etContact);
        etPhone = findViewById(R.id.etPhone);
        btnLogin = findViewById(R.id.btnLogin);
        tvStatus = findViewById(R.id.tvStatus);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                tvStatus.setText("⚠️ Vui lòng nhập tài khoản và mật khẩu");
                return;
            }

            usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Tài khoản tồn tại → kiểm tra mật khẩu
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.password.equals(password)) {
                            tvStatus.setText("✅ Đăng nhập thành công\n📍 Địa chỉ: " + user.contact + "\n📞 SĐT: " + user.phone);
                        } else {
                            tvStatus.setText("❌ Sai mật khẩu!");
                        }
                    } else {
                        // Tài khoản chưa tồn tại → tạo mới
                        User newUser = new User(password, contact, phone);
                        usersRef.child(username).setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    tvStatus.setText("✅ Tài khoản mới đã được tạo!\n📍 " + contact + "\n📞 " + phone);
                                })
                                .addOnFailureListener(e -> {
                                    tvStatus.setText("❌ Tạo tài khoản thất bại: " + e.getMessage());
                                    Log.e("FIREBASE", "Tạo tài khoản thất bại", e);
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    tvStatus.setText("⚠️ Lỗi Firebase: " + error.getMessage());
                }
            });
        });
    }
}
