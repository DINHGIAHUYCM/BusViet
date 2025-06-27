package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etPhone;
    Button btnSignUp;
    CheckBox cbAgree;
    TextView tvAlreadyHaveAccount;

    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        btnSignUp = findViewById(R.id.btnSignUp);
        cbAgree = findViewById(R.id.cbAgree);
        tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnSignUp.setOnClickListener(view -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            // --- VALIDATION ---
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "❌ Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "❌ Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phone.matches("^0[0-9]{9}$")) {
                Toast.makeText(this, "❌ Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbAgree.isChecked()) {
                Toast.makeText(this, "❗ Bạn cần đồng ý với điều khoản dịch vụ", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- KIỂM TRA TRÙNG EMAIL / PHONE ---
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean emailExists = false;
                    boolean phoneExists = false;

                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        User user = userSnap.getValue(User.class);
                        if (user != null) {
                            if (user.email != null && user.email.equalsIgnoreCase(email)) {
                                emailExists = true;
                            }
                            if (user.phone != null && user.phone.equals(phone)) {
                                phoneExists = true;
                            }
                        }
                    }

                    if (emailExists) {
                        Toast.makeText(RegisterActivity.this, "⚠️ Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                    } else if (phoneExists) {
                        Toast.makeText(RegisterActivity.this, "⚠️ Số điện thoại đã được sử dụng", Toast.LENGTH_SHORT).show();
                    } else {
                        // --- ĐĂNG KÝ ---
                        User newUser = new User(password, email, phone, name);

                        String userId = usersRef.push().getKey(); // Tạo ID tự động

                        if (userId != null) {
                            usersRef.child(userId).setValue(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "✅ Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, SuccessActivity.class));
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("FIREBASE", "Tạo tài khoản thất bại", e);
                                    });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(RegisterActivity.this, "❌ Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvAlreadyHaveAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}
