package com.busviet;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class TicketDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        // Nhận dữ liệu vé từ Intent
        Ticket ticket = (Ticket) getIntent().getSerializableExtra("ticket");
        Bus bus = (Bus) getIntent().getSerializableExtra("bus");

        if (ticket != null && bus != null) {
            // Hiển thị tất cả thông tin vé
            TextView txtTicketType = findViewById(R.id.txtDetailTicketType);
            TextView txtRouteCode = findViewById(R.id.txtDetailRouteCode);
            TextView txtPurchaseDate = findViewById(R.id.txtDetailPurchaseDate);
            TextView txtExpireDate = findViewById(R.id.txtDetailExpireDate);
            TextView txtTicketCount = findViewById(R.id.txtDetailTicketCount);
            TextView txtTotalPrice = findViewById(R.id.txtDetailTotalPrice);
            TextView txtIsValid = findViewById(R.id.txtDetailIsValid);
            TextView txtValidInMonth = findViewById(R.id.txtDetailValidInMonth);
            TextView txtPurchaseMonth = findViewById(R.id.txtDetailPurchaseMonth);
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
            txtIsValid.setText("Còn hiệu lực: " + (ticket.isValid ? "Có" : "Không"));
            txtValidInMonth.setText("Hiệu lực tháng này: " + (ticket.validInCurrentMonth ? "Có" : "Không"));
            txtPurchaseMonth.setText("Tháng mua: " + ticket.purchaseMonth);
            txtUsername.setText("Người mua: " + ticket.username);
            txtRoute.setText(bus.startPoint + " ➜ " + bus.endPoint);
            txtStatus.setText("Trạng thái: " + (bus.active ? "Hoạt động" : "Ngừng"));
        }

        // Nút quay lại
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

    }
}