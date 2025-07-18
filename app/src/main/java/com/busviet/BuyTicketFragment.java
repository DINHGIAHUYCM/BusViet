package com.busviet;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.*;
import java.text.NumberFormat;
import java.util.*;

public class BuyTicketFragment extends Fragment {
    
    private String username, role;
    private Spinner spinnerRoute, spinnerTicketType;
    private EditText etTicketCount;
    private TextView tvTotalPrice, tvStatus;
    private Button btnBuyTicket;
    private DatabaseReference busesRef, purchasesRef;
    private List<Bus> busList;

    // Pricing theo yêu cầu
    private static final int PRICE_SINGLE = 10000;    // 7k/lượt
    private static final int PRICE_DAILY = 20000;    // 20k/ngày  
    private static final int PRICE_MONTHLY = 140000; // 140k/tháng
    private static final int PRICE_QUARTERLY = 400000; // 400k/quý
    private static final int PRICE_YEARLY = 1500000; // 1500k/năm

    public BuyTicketFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buy_ticket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Nhận arguments
        Bundle args = getArguments();
        if (args != null) {
            username = args.getString("username");
            role = args.getString("role");
        }

        // Initialize views
        spinnerRoute = view.findViewById(R.id.spinnerRoute);
        spinnerTicketType = view.findViewById(R.id.spinnerTicketType);
        etTicketCount = view.findViewById(R.id.etTicketCount);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvStatus = view.findViewById(R.id.tvStatus);
        btnBuyTicket = view.findViewById(R.id.btnBuyTicket);

        // Initialize Firebase references
        busesRef = FirebaseDatabase.getInstance().getReference("bus"); // Đổi từ "buses" thành "bus"
        purchasesRef = FirebaseDatabase.getInstance().getReference("purchases");

        setupTicketTypeSpinner();
        loadBusRoutes();
        setupEventListeners();
    }

    private void setupTicketTypeSpinner() {
        String[] ticketTypes = {
            "🎫 Vé lượt (10,000₫)",
            "🌅 Vé ngày (20,000₫)", 
            "📅 Vé tháng (140,000₫)",
            "📆 Vé quý (400,000₫)",
            "🗓️ Vé năm (1,500,000₫)"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, ticketTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTicketType.setAdapter(adapter);
    }

    private void loadBusRoutes() {
        tvStatus.setText("🔄 Đang tải danh sách tuyến...");
        
        busesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                busList = new ArrayList<>();
                List<String> routeDisplayList = new ArrayList<>();

                for (DataSnapshot busSnapshot : snapshot.getChildren()) {
                    try {
                        Bus bus = busSnapshot.getValue(Bus.class);
                        if (bus != null && bus.routeCode != null && bus.startPoint != null && bus.endPoint != null) {
                            busList.add(bus);
                            String routeDisplay = bus.routeCode + " - " + bus.startPoint + " → " + bus.endPoint;
                            routeDisplayList.add(routeDisplay);
                        } else {
                            // Fallback: lấy từng field
                            String routeCode = busSnapshot.child("routeCode").getValue(String.class);
                            String startPoint = busSnapshot.child("startPoint").getValue(String.class);
                            String endPoint = busSnapshot.child("endPoint").getValue(String.class);
                            
                            if (routeCode != null && startPoint != null && endPoint != null) {
                                Bus newBus = new Bus();
                                newBus.id = busSnapshot.getKey();
                                newBus.routeCode = routeCode;
                                newBus.startPoint = startPoint;
                                newBus.endPoint = endPoint;
                                newBus.ticketPrice = busSnapshot.child("ticketPrice").getValue(Integer.class) != null ? 
                                    busSnapshot.child("ticketPrice").getValue(Integer.class) : 10000;
                                newBus.active = true;
                                
                                busList.add(newBus);
                                String routeDisplay = routeCode + " - " + startPoint + " → " + endPoint;
                                routeDisplayList.add(routeDisplay);
                            }
                        }
                    } catch (Exception e) {
                        // Bỏ qua nếu có lỗi
                    }
                }

                if (routeDisplayList.isEmpty()) {
                    routeDisplayList.add("Chưa có tuyến nào");
                    tvStatus.setText("⚠️ Không tìm thấy tuyến nào");
                } else {
                    tvStatus.setText("✅ Đã tải " + routeDisplayList.size() + " tuyến xe");
                }

                ArrayAdapter<String> routeAdapter = new ArrayAdapter<>(getContext(), 
                    android.R.layout.simple_spinner_item, routeDisplayList);
                routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoute.setAdapter(routeAdapter);

                updateTotalPrice();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvStatus.setText("❌ Lỗi tải tuyến: " + error.getMessage());
            }
        });
    }

    private void setupEventListeners() {
        spinnerRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTotalPrice();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTicketType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTotalPrice();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etTicketCount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateTotalPrice();
            }
        });

        btnBuyTicket.setOnClickListener(v -> processPurchase());
    }

    private void updateTotalPrice() {
        try {
            String countStr = etTicketCount.getText().toString().trim();
            int ticketCount = countStr.isEmpty() ? 1 : Integer.parseInt(countStr);
            
            int ticketPrice = getTicketPrice(spinnerTicketType.getSelectedItemPosition());
            long totalPrice = (long) ticketPrice * ticketCount;
            
            tvTotalPrice.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalPrice));
        } catch (NumberFormatException e) {
            tvTotalPrice.setText("Tổng tiền: 0 ₫");
        }
    }

    private int getTicketPrice(int typePosition) {
        switch (typePosition) {
            case 0: return PRICE_SINGLE;
            case 1: return PRICE_DAILY;
            case 2: return PRICE_MONTHLY;
            case 3: return PRICE_QUARTERLY;
            case 4: return PRICE_YEARLY;
            default: return PRICE_SINGLE;
        }
    }

    private String getTicketTypeKey(int position) {
        switch (position) {
            case 0: return "single";
            case 1: return "daily";
            case 2: return "monthly";
            case 3: return "quarterly";
            case 4: return "yearly";
            default: return "single";
        }
    }

    private long calculateExpireDate(String ticketType, long purchaseDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(purchaseDate);
        
        switch (ticketType) {
            case "single":
                calendar.add(Calendar.HOUR, 2); // Vé lượt hết hạn sau 2 tiếng
                break;
            case "daily":
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case "monthly":
                calendar.add(Calendar.MONTH, 1);
                break;
            case "quarterly":
                calendar.add(Calendar.MONTH, 3);
                break;
            case "yearly":
                calendar.add(Calendar.YEAR, 1);
                break;
        }
        
        return calendar.getTimeInMillis();
    }

    private void processPurchase() {
        if (busList == null || busList.isEmpty()) {
            tvStatus.setText("❌ Chưa có tuyến nào để mua vé");
            return;
        }

        try {
            String countStr = etTicketCount.getText().toString().trim();
            int ticketCount = countStr.isEmpty() ? 1 : Integer.parseInt(countStr);
            
            if (ticketCount <= 0) {
                tvStatus.setText("❌ Số lượng vé phải lớn hơn 0");
                return;
            }

            int routePosition = spinnerRoute.getSelectedItemPosition();
            if (routePosition < 0 || routePosition >= busList.size()) {
                tvStatus.setText("❌ Vui lòng chọn tuyến xe hợp lệ");
                return;
            }

            Bus selectedBus = busList.get(routePosition);
            int typePosition = spinnerTicketType.getSelectedItemPosition();
            String ticketType = getTicketTypeKey(typePosition);
            int ticketPrice = getTicketPrice(typePosition);
            
            long currentTime = System.currentTimeMillis();
            long expireTime = calculateExpireDate(ticketType, currentTime);
            int totalPrice = ticketPrice * ticketCount;
            // Tạo purchase object
            VNPay.requestPaymentUrl(requireContext(), totalPrice, new VNPay.PaymentCallback() {
                @Override
                public void onSuccess(String paymentUrl) {

                    Intent intent = new Intent(requireContext(), PaymentActivity.class);
                    intent.putExtra("paymentUrl", paymentUrl);

                    startActivityForResult(intent, 1234); // mở webview thanh toán
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });




        } catch (NumberFormatException e) {
            tvStatus.setText("❌ Số lượng vé không hợp lệ");
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            if (resultCode == PaymentActivity.RESULT_OK) {

                if (etTicketCount == null || spinnerRoute == null || spinnerTicketType == null || busList == null || busList.isEmpty()) {
                    Toast.makeText(requireContext(), "Dữ liệu không hợp lệ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String countStr = etTicketCount.getText().toString().trim();
                int ticketCount = countStr.isEmpty() ? 1 : Integer.parseInt(countStr);

                int routePosition = spinnerRoute.getSelectedItemPosition();
                if (routePosition < 0 || routePosition >= busList.size()) {
                    Toast.makeText(requireContext(), "Tuyến xe không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bus selectedBus = busList.get(routePosition);

                int typePosition = spinnerTicketType.getSelectedItemPosition();
                String ticketType = getTicketTypeKey(typePosition);
                int ticketPrice = getTicketPrice(typePosition);

                long currentTime = System.currentTimeMillis();
                long expireTime = calculateExpireDate(ticketType, currentTime);
                int totalPrice = ticketPrice * ticketCount;

                savePurchaseToFirebase(
                        username,
                        selectedBus.routeCode,
                        ticketType,
                        ticketCount,
                        currentTime,
                        expireTime,
                        totalPrice
                );
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Thanh toán thất bại hoặc bị huỷ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void savePurchaseToFirebase(
            String username,
            String routeCode,
            String ticketType,
            int ticketCount,
            long purchaseTime,
            long expireTime,
            int totalPrice
    ) {
        Purchase purchase = new Purchase(
                username,
                routeCode,
                ticketType,
                ticketCount,
                purchaseTime,
                expireTime,
                totalPrice,
                true
        );

        String purchaseId = purchasesRef.push().getKey();
        if (purchaseId != null) {
            purchasesRef.child(purchaseId).setValue(purchase)
                    .addOnSuccessListener(aVoid -> {
                        tvStatus.setText("✅ Mua vé thành công!");
                        etTicketCount.setText("1");
                        spinnerTicketType.setSelection(0);
                        updateTotalPrice();
                    })
                    .addOnFailureListener(e -> {
                        tvStatus.setText("❌ Lỗi mua vé: " + e.getMessage());
                    });
        }
    }

    private String getTicketTypeName(int position) {
        switch (position) {
            case 0: return "Vé lượt";
            case 1: return "Vé ngày";
            case 2: return "Vé tháng";
            case 3: return "Vé quý";
            case 4: return "Vé năm";
            default: return "Vé lượt";
        }
    }
}
