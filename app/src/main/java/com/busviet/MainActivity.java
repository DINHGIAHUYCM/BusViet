package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

// MainActivity.java
public class MainActivity extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvStatus, tvGoToRegister;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvStatus = findViewById(R.id.tvStatus);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                tvStatus.setText("⚠️ Nhập tài khoản và mật khẩu");
                return;
            }

            usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.password.equals(password)) {
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("role", user.role);
                            startActivity(intent);
                            finish();
                        } else {
                            tvStatus.setText("❌ Sai mật khẩu!");
                        }
                    } else {
                        tvStatus.setText("❌ Tài khoản không tồn tại.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    tvStatus.setText("⚠️ Lỗi Firebase: " + error.getMessage());
                }
            });
        });

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }
}

