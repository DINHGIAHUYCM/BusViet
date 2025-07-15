package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class MainActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvStatus;
    ImageView ivTogglePassword;

    DatabaseReference usersRef;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvStatus = findViewById(R.id.tvStatus);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = "Customer"; // Mặc định

            if (username.isEmpty() || password.isEmpty()) {
                tvStatus.setText("⚠️ Vui lòng nhập tài khoản và mật khẩu");
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
                        User newUser = new User(password, "", "", role);
                        usersRef.child(username).setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    tvStatus.setText("✅ Tạo tài khoản mới với quyền Customer");
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
