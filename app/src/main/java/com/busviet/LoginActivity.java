package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {


    EditText etPhone, etPassword;
    Button btnLogin;
    TextView tvCreateAccount, tvStatus;
    CheckBox cbRememberMe;

    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvStatus = findViewById(R.id.tvStatus); // Optional: để hiển thị trạng thái
        cbRememberMe = findViewById(R.id.cbRememberMe);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(view -> {
            String inputPhone = etPhone.getText().toString().trim();  // etUsername = nhập số điện thoại
            String inputPassword = etPassword.getText().toString().trim();

            if (inputPhone.isEmpty() || inputPassword.isEmpty()) {
                tvStatus.setText("⚠️ Vui lòng nhập số điện thoại và mật khẩu");
                return;
            }

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean found = false;

                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        User user = userSnap.getValue(User.class);

                        if (user != null && user.phone != null && user.phone.equals(inputPhone)) {
                            found = true;

                            if (user.password != null && user.password.equals(inputPassword)) {
                                Toast.makeText(LoginActivity.this, "✅ Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, SuccessActivity.class));
                            } else {
                                tvStatus.setText("❌ Sai mật khẩu!");
                            }
                            break;
                        }
                    }

                    if (!found) {
                        tvStatus.setText("❌ Số điện thoại không tồn tại!");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    tvStatus.setText("❌ Lỗi đăng nhập: " + error.getMessage());
                }
            });
        });

        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}