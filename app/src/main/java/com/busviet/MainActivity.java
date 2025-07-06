package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class MainActivity extends AppCompatActivity {

    EditText etUsername, etPassword, etContact, etPhone;
    Button btnLogin;
    TextView tvStatus;
    RadioGroup rgRole;
    RadioButton rbAdmin, rbCustomer;
    ImageView ivTogglePassword;

    DatabaseReference usersRef;
    boolean isPasswordVisible = false;

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
        rgRole = findViewById(R.id.rgRole);
        rbAdmin = findViewById(R.id.rbAdmin);
        rbCustomer = findViewById(R.id.rbCustomer);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_open); // icon mắt mở
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_closed); // icon mắt đóng
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String role = rbAdmin.isChecked() ? "Admin" : "Customer";

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
                            intent.putExtra("role", user.role); // truyền role
                            startActivity(intent);
                            finish();
                        } else {
                            tvStatus.setText("❌ Sai mật khẩu!");
                        }
                    } else {
                        User newUser = new User(password, contact, phone, role);
                        usersRef.child(username).setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    tvStatus.setText("✅ Đã tạo tài khoản mới với quyền " + role);
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
