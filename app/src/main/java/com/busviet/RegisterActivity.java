package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// RegisterActivity.java
public class RegisterActivity extends AppCompatActivity {
    EditText etUsername, etPassword, etContact, etPhone;
    Button btnRegister;
    TextView tvStatus, tvGoToLogin;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etContact = findViewById(R.id.etContact);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        tvStatus = findViewById(R.id.tvStatus);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                tvStatus.setText("⚠️ Vui lòng nhập đủ thông tin!");
                return;
            }

            usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        tvStatus.setText("❌ Tài khoản đã tồn tại!");
                    } else {
                        User newUser = new User(password, contact, phone, "Customer");
                        usersRef.child(username).setValue(newUser)
                                .addOnSuccessListener(aVoid -> tvStatus.setText("✅ Tạo tài khoản thành công!"))
                                .addOnFailureListener(e -> tvStatus.setText("❌ Thất bại: " + e.getMessage()));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    tvStatus.setText("⚠️ Lỗi Firebase: " + error.getMessage());
                }
            });
        });

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }
}

