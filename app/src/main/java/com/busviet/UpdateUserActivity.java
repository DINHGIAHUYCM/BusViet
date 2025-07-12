package com.busviet;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.busviet.MainActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateUserActivity extends AppCompatActivity {

    EditText editName, editEmail, editPhone, editContact, editPassword, editRePassword;
    Button buttonUpdate, buttonBack;

    ImageView showPass, showRePass;

    FirebaseAuth auth;
    DatabaseReference userRef;

    boolean showPwd = false, showRePwd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        //editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editContact = findViewById(R.id.editContact);
        //editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editRePassword = findViewById(R.id.editRePassword);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonBack = findViewById(R.id.buttonBack);

        showPass = findViewById(R.id.showPass);
        showRePass = findViewById(R.id.showRePass);

        auth = FirebaseAuth.getInstance();
        String username = getIntent().getStringExtra("username");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(username);




        loadUserData();

        showPass.setOnClickListener(v -> togglePasswordVisibility(editPassword, showPass, true));
        showRePass.setOnClickListener(v -> togglePasswordVisibility(editRePassword, showRePass, false));

        buttonUpdate.setOnClickListener(v -> {
            //String name = editName.getText().toString();
            //String email = editEmail.getText().toString();
            String phone = editPhone.getText().toString();
            String contact = editContact.getText().toString();
            String pass = editPassword.getText().toString();
            String rePass = editRePassword.getText().toString();

            if (!pass.equals(rePass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            //userRef.child("name").setValue(name);
            //userRef.child("email").setValue(email);
            userRef.child("contact").setValue(contact);
            userRef.child("phone").setValue(phone);
            userRef.child("password").setValue(pass);

            Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        });

        buttonBack.setOnClickListener(v -> finish());

    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //editName.setText(snapshot.child("name").getValue(String.class));
                //editEmail.setText(snapshot.child("email").getValue(String.class));
                editContact.setText(snapshot.child("contact").getValue(String.class));
                editPhone.setText(snapshot.child("phone").getValue(String.class));
                editPassword.setText(snapshot.child("password").getValue(String.class));
                editRePassword.setText(snapshot.child("password").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView icon, boolean isMain) {
        boolean visible = isMain ? showPwd : showRePwd;
        if (visible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            icon.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            icon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
        editText.setSelection(editText.getText().length());
        if (isMain) showPwd = !showPwd; else showRePwd = !showRePwd;
    }
}
