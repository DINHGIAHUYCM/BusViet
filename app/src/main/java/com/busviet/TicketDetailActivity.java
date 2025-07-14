package com.busviet;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class TicketDetailActivity extends AppCompatActivity {

    private Purchase ticket; // Lưu tạm để truy cập trong nhiều nơi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        // Nhận dữ liệu từ Intent
        ticket = (Purchase) getIntent().getSerializableExtra("ticket");

        Bus bus = (Bus) getIntent().getSerializableExtra("bus");


        if (ticket == null) {
            Toast.makeText(this, "Không nhận được thông tin vé!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (ticket != null && bus != null) {
            // Hiển thị thông tin vé
            TextView txtTicketType = findViewById(R.id.txtDetailTicketType);
            TextView txtRouteCode = findViewById(R.id.txtDetailRouteCode);
            TextView txtPurchaseDate = findViewById(R.id.txtDetailPurchaseDate);
            TextView txtExpireDate = findViewById(R.id.txtDetailExpireDate);
            TextView txtTicketCount = findViewById(R.id.txtDetailTicketCount);
            TextView txtTotalPrice = findViewById(R.id.txtDetailTotalPrice);

            TextView txtUsername = findViewById(R.id.txtDetailUsername);
            TextView txtRoute = findViewById(R.id.txtRoute);
            TextView txtStatus = findViewById(R.id.txtStatus);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            txtTicketType.setText("Loại vé: " + ticket.ticketType);
            txtRouteCode.setText("Tuyến: " + ticket.routeCode);
            txtPurchaseDate.setText("Ngày mua: " + sdf.format(new Date(ticket.purchaseDate)));
            txtExpireDate.setText("Ngày hết hạn: " + sdf.format(new Date(ticket.expireDate)));
            txtTicketCount.setText("Số lượng vé còn lại: " + ticket.ticketCount);
            txtTotalPrice.setText("Tổng giá: " + ticket.totalPrice + " VND");

            txtUsername.setText("Người mua: " + ticket.username);
            txtRoute.setText(bus.startPoint + " ➜ " + bus.endPoint);
            txtStatus.setText("Trạng thái: " + (bus.active ? "Hoạt động" : "Ngừng"));
        }

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnRipeTicket = findViewById(R.id.btnRipeTicket);
        btnRipeTicket.setOnClickListener(v -> ripTicket());
    }

    private void ripTicket() {
        if (ticket.ticketCount <= 0) {
            Toast.makeText(this, "Không thể xé vé vì số lượng còn lại là 0", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xé vé")
                .setMessage("Bạn có chắc chắn muốn xé vé này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    DatabaseReference purchasesRef = FirebaseDatabase.getInstance()
                            .getReference("purchases");

                    purchasesRef.orderByChild("username")
                            .equalTo(ticket.username)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                                        Purchase found = ticketSnapshot.getValue(Purchase.class);

                                        if (found != null
                                                && found.routeCode.equals(ticket.routeCode)
                                                && found.ticketType.equals(ticket.ticketType)) {

                                            DatabaseReference targetRef = ticketSnapshot.getRef();

                                            targetRef.runTransaction(new Transaction.Handler() {
                                                @NonNull
                                                @Override
                                                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                                    Purchase t = currentData.getValue(Purchase.class);
                                                    if (t == null) return Transaction.success(currentData);

                                                    if (t.ticketCount > 0) {
                                                        t.ticketCount -= 1;
                                                        currentData.setValue(t);
                                                    }
                                                    return Transaction.success(currentData);
                                                }

                                                @Override
                                                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                                    if (committed && currentData != null) {
                                                        Purchase updated = currentData.getValue(Purchase.class);
                                                        if (updated != null) {
                                                            ticket.ticketCount = updated.ticketCount;

                                                            // ✅ Cập nhật trực tiếp lại giao diện thay vì recreate()
                                                            TextView txtTicketCount = findViewById(R.id.txtDetailTicketCount);
                                                            txtTicketCount.setText("Số lượng vé còn lại: " + ticket.ticketCount);

                                                            Toast.makeText(TicketDetailActivity.this, "Xé vé thành công!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(TicketDetailActivity.this, "Xé vé thất bại!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(TicketDetailActivity.this, "Lỗi đọc dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Không", null)
                .show();
    }

}
