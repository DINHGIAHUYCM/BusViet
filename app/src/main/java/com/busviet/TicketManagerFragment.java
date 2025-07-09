package com.busviet;

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
import java.text.SimpleDateFormat;
import java.util.*;

public class TicketManagerFragment extends Fragment {
    
    private String username, role;
    private LinearLayout layoutContent;
    private DatabaseReference purchasesRef, usersRef;
    private TextView tvTitle;

    public TicketManagerFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket_manager, container, false);
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
        layoutContent = view.findViewById(R.id.layoutContent);
        tvTitle = view.findViewById(R.id.tvTitle);

        // Initialize Firebase references
        purchasesRef = FirebaseDatabase.getInstance().getReference("purchases");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        if ("Admin".equals(role)) {
            tvTitle.setText("📊 Quản lý & Thống kê vé (Admin)");
            showAdminInterface();
        } else {
            tvTitle.setText("🎫 Vé của tôi");
            showUserTickets();
        }
    }

    private void showAdminInterface() {
        createMonthSelector();
        loadOverallStatistics();
    }

    private void createMonthSelector() {
        LinearLayout monthContainer = new LinearLayout(getContext());
        monthContainer.setOrientation(LinearLayout.HORIZONTAL);
        monthContainer.setPadding(32, 16, 32, 16);
        monthContainer.setGravity(android.view.Gravity.CENTER);

        TextView tvMonthLabel = new TextView(getContext());
        tvMonthLabel.setText("Chọn tháng để xem chi tiết: ");
        tvMonthLabel.setTextSize(14);

        Spinner spinnerMonth = new Spinner(getContext());
        
        // Tạo danh sách tháng (6 tháng gần nhất)
        List<String> months = new ArrayList<>();
        List<String> monthValues = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat displaySdf = new SimpleDateFormat("MM/yyyy");
        
        for (int i = 0; i < 6; i++) {
            String monthValue = sdf.format(calendar.getTime());
            String monthDisplay = displaySdf.format(calendar.getTime());
            months.add(monthDisplay);
            monthValues.add(monthValue);
            calendar.add(Calendar.MONTH, -1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonth = monthValues.get(position);
                loadMonthlyStatistics(selectedMonth);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        monthContainer.addView(tvMonthLabel);
        monthContainer.addView(spinnerMonth);
        layoutContent.addView(monthContainer);

        // Divider
        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(0xFFCCCCCC);
        LinearLayout.LayoutParams dividerParams = (LinearLayout.LayoutParams) divider.getLayoutParams();
        dividerParams.setMargins(32, 16, 32, 16);
        layoutContent.addView(divider);
    }

    private void loadOverallStatistics() {
        purchasesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> typeStats = new HashMap<>();
                Set<String> uniqueUsers = new HashSet<>();
                long totalRevenue = 0;
                int totalTickets = 0;

                for (DataSnapshot purchaseSnapshot : snapshot.getChildren()) {
                    Purchase purchase = purchaseSnapshot.getValue(Purchase.class);
                    if (purchase != null) {
                        uniqueUsers.add(purchase.username);
                        totalTickets += purchase.ticketCount;
                        totalRevenue += purchase.totalPrice;
                        
                        String typeKey = purchase.ticketType;
                        typeStats.put(typeKey, typeStats.getOrDefault(typeKey, 0) + purchase.ticketCount);
                    }
                }

                displayOverallStats(uniqueUsers.size(), totalTickets, totalRevenue, typeStats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showErrorMessage("Lỗi tải dữ liệu: " + error.getMessage());
            }
        });
    }

    private void loadMonthlyStatistics(String selectedMonth) {
        // Clear monthly data trước đó
        clearMonthlyData();

        purchasesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Purchase> monthlyPurchases = new ArrayList<>();
                Set<String> monthlyUsers = new HashSet<>();
                Set<String> validUsers = new HashSet<>();

                for (DataSnapshot purchaseSnapshot : snapshot.getChildren()) {
                    Purchase purchase = purchaseSnapshot.getValue(Purchase.class);
                    if (purchase != null && selectedMonth.equals(purchase.getPurchaseMonth())) {
                        monthlyPurchases.add(purchase);
                        monthlyUsers.add(purchase.username);
                        
                        if (purchase.isValidInCurrentMonth()) {
                            validUsers.add(purchase.username);
                        }
                    }
                }

                displayMonthlyStats(selectedMonth, monthlyPurchases, validUsers);
                displayValidUsersInMonth(selectedMonth, validUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showErrorMessage("Lỗi tải dữ liệu tháng: " + error.getMessage());
            }
        });
    }

    private void clearMonthlyData() {
        int childCount = layoutContent.getChildCount();
        for (int i = childCount - 1; i >= 3; i--) { // Keep first 3 views
            layoutContent.removeViewAt(i);
        }
    }

    private void displayOverallStats(int totalUsers, int totalTickets, long totalRevenue, Map<String, Integer> typeStats) {
        LinearLayout container = createStatsContainer("📈 Thống kê tổng quan");

        addStatItem(container, "👥 Tổng số người dùng đã mua vé", String.valueOf(totalUsers));
        addStatItem(container, "🎫 Tổng số vé đã bán", String.valueOf(totalTickets));
        addStatItem(container, "💰 Tổng doanh thu", NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalRevenue));

        if (!typeStats.isEmpty()) {
            addStatHeader(container, "📊 Phân loại vé (tổng quan)");
            // Thêm chart với progress bars
            addTicketChart(container, typeStats, totalTickets);
        }

        layoutContent.addView(container);
    }

    private void displayMonthlyStats(String month, List<Purchase> purchases, Set<String> validUsers) {
        LinearLayout container = createStatsContainer("📅 Thống kê tháng " + formatMonth(month));

        int totalTickets = 0;
        long totalRevenue = 0;
        Map<String, Integer> typeCount = new HashMap<>();
        Set<String> allMonthlyUsers = new HashSet<>();

        for (Purchase purchase : purchases) {
            totalTickets += purchase.ticketCount;
            totalRevenue += purchase.totalPrice;
            typeCount.put(purchase.ticketType, typeCount.getOrDefault(purchase.ticketType, 0) + purchase.ticketCount);
            allMonthlyUsers.add(purchase.username);
        }

        // Thống kê cơ bản
        addStatItem(container, "👥 Người dùng có vé hợp lệ trong tháng", String.valueOf(validUsers.size()));
        addStatItem(container, "📊 Tổng số người đã mua vé trong tháng", String.valueOf(allMonthlyUsers.size()));
        addStatItem(container, "🛒 Tổng lượt mua vé", String.valueOf(purchases.size()));
        addStatItem(container, "🎫 Tổng số vé bán ra", String.valueOf(totalTickets));
        addStatItem(container, "💰 Doanh thu tháng", NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalRevenue));

        // Phân loại vé theo tháng
        if (!typeCount.isEmpty()) {
            addStatHeader(container, "📊 Số vé được mua theo từng loại");
            // Thêm chart với progress bars cho tháng
            addTicketChart(container, typeCount, totalTickets);
        }

        layoutContent.addView(container);
    }

    private void displayValidUsersInMonth(String month, Set<String> validUsers) {
        if (validUsers.isEmpty()) {
            LinearLayout container = createStatsContainer("👥 Không có người dùng nào có vé hợp lệ trong tháng này");
            layoutContent.addView(container);
            return;
        }

        LinearLayout container = createStatsContainer("👥 Danh sách " + validUsers.size() + " người dùng có vé hợp lệ trong tháng");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> userList = new ArrayList<>(validUsers);
                Collections.sort(userList);

                for (String userId : userList) {
                    DataSnapshot userSnapshot = snapshot.child(userId);
                    if (userSnapshot.exists()) {
                        User user = userSnapshot.getValue(User.class);
                        addUserItem(container, userId, user);
                    } else {
                        addUserItem(container, userId, null);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                addStatItem(container, "❌ Lỗi tải thông tin người dùng", error.getMessage());
            }
        });

        layoutContent.addView(container);
    }

    // USER TICKETS IMPLEMENTATION
    private void showUserTickets() {
        // Load user's tickets from Firebase
        loadUserTickets();
    }

    private void loadUserTickets() {
        purchasesRef.orderByChild("username").equalTo(username)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    layoutContent.removeAllViews();
                    
                    List<Purchase> userPurchases = new ArrayList<>();
                    for (DataSnapshot purchaseSnapshot : snapshot.getChildren()) {
                        Purchase purchase = purchaseSnapshot.getValue(Purchase.class);
                        if (purchase != null) {
                            userPurchases.add(purchase);
                        }
                    }
                    
                    if (userPurchases.isEmpty()) {
                        showNoTicketsMessage();
                    } else {
                        displayUserTickets(userPurchases);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showErrorMessage("Lỗi tải dữ liệu vé: " + error.getMessage());
                }
            });
    }

    private void showNoTicketsMessage() {
        LinearLayout container = createStatsContainer("🎫 Vé của bạn");
        
        TextView noTicketsView = new TextView(getContext());
        noTicketsView.setText("📝 Bạn chưa mua vé nào.\n\n" +
                "💡 Hãy chuyển sang tab 'Mua vé' để mua vé xe buýt!");
        noTicketsView.setTextSize(16);
        noTicketsView.setTextColor(0xFF666666);
        noTicketsView.setGravity(android.view.Gravity.CENTER);
        noTicketsView.setPadding(32, 32, 32, 32);
        
        container.addView(noTicketsView);
        layoutContent.addView(container);
    }

    private void displayUserTickets(List<Purchase> purchases) {
        // Sort by purchase date (newest first)
        purchases.sort((p1, p2) -> Long.compare(p2.purchaseDate, p1.purchaseDate));
        
        // Create summary container
        createUserTicketSummary(purchases);
        
        // Create tickets list
        createUserTicketsList(purchases);
    }

    private void createUserTicketSummary(List<Purchase> purchases) {
        LinearLayout container = createStatsContainer("📊 Tổng quan vé của bạn");
        
        int totalTickets = 0;
        int validTickets = 0;
        long totalSpent = 0;
        Map<String, Integer> typeCount = new HashMap<>();
        
        for (Purchase purchase : purchases) {
            totalTickets += purchase.ticketCount;
            totalSpent += purchase.totalPrice;
            
            if (purchase.isValidInCurrentMonth()) {
                validTickets += purchase.ticketCount;
            }
            
            typeCount.put(purchase.ticketType, 
                typeCount.getOrDefault(purchase.ticketType, 0) + purchase.ticketCount);
        }
        
        addStatItem(container, "🎫 Tổng số vé đã mua", String.valueOf(totalTickets));
        addStatItem(container, "✅ Vé còn hiệu lực", String.valueOf(validTickets));
        addStatItem(container, "💰 Tổng chi tiêu", 
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalSpent));
        addStatItem(container, "🛒 Số lần mua vé", String.valueOf(purchases.size()));
        
        // Add ticket type breakdown
        if (!typeCount.isEmpty()) {
            addStatHeader(container, "📋 Phân loại vé đã mua");
            addTicketChart(container, typeCount, totalTickets);
        }
        
        layoutContent.addView(container);
    }

    private void createUserTicketsList(List<Purchase> purchases) {
        LinearLayout container = createStatsContainer("🎫 Danh sách vé của bạn");
        
        for (Purchase purchase : purchases) {
            addUserTicketItem(container, purchase);
        }
        
        layoutContent.addView(container);
    }

    private void addUserTicketItem(LinearLayout container, Purchase purchase) {
        LinearLayout ticketLayout = new LinearLayout(getContext());
        ticketLayout.setOrientation(LinearLayout.VERTICAL);
        ticketLayout.setPadding(16, 12, 16, 12);
        
        // Determine ticket status and color
        boolean isValid = purchase.isValidInCurrentMonth();
        int backgroundColor = isValid ? 0xFFE8F5E8 : 0xFFFFE8E8;
        
        ticketLayout.setBackgroundColor(backgroundColor);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        ticketLayout.setLayoutParams(params);
        
        // Header with ticket type and status
        LinearLayout headerLayout = new LinearLayout(getContext());
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        TextView typeView = new TextView(getContext());
        typeView.setText(getTicketTypeDisplay(purchase.ticketType));
        typeView.setTextSize(16);
        typeView.setTextColor(0xFF333333);
        typeView.setTypeface(null, android.graphics.Typeface.BOLD);
        typeView.setLayoutParams(new LinearLayout.LayoutParams(0, 
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        
        TextView statusView = new TextView(getContext());
        statusView.setText(isValid ? "✅ Còn hiệu lực" : "❌ Hết hạn");
        statusView.setTextSize(14);
        statusView.setTextColor(isValid ? 0xFF4CAF50 : 0xFFFF5722);
        statusView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        headerLayout.addView(typeView);
        headerLayout.addView(statusView);
        
        // Ticket details
        TextView detailsView = new TextView(getContext());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        String details = "🚌 Tuyến: " + purchase.routeCode +
                "\n🎫 Số lượng: " + purchase.ticketCount + " vé" +
                "\n💰 Giá: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(purchase.totalPrice) +
                "\n📅 Ngày mua: " + sdf.format(new Date(purchase.purchaseDate)) +
                "\n⏰ Hết hạn: " + sdf.format(new Date(purchase.expireDate));
        
        detailsView.setText(details);
        detailsView.setTextSize(13);
        detailsView.setTextColor(0xFF666666);
        detailsView.setPadding(0, 8, 0, 0);
        
        ticketLayout.addView(headerLayout);
        ticketLayout.addView(detailsView);
        
        container.addView(ticketLayout);
    }

    // HELPER METHODS
    private LinearLayout createStatsContainer(String title) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 16, 24, 16);
        container.setBackgroundColor(0xFFF9F9F9);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 8, 16, 8);
        container.setLayoutParams(params);

        TextView titleView = new TextView(getContext());
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(0, 0, 0, 12);
        container.addView(titleView);

        return container;
    }

    private void addStatHeader(LinearLayout container, String header) {
        TextView headerView = new TextView(getContext());
        headerView.setText(header);
        headerView.setTextSize(14);
        headerView.setTypeface(null, android.graphics.Typeface.BOLD);
        headerView.setPadding(0, 12, 0, 6);
        container.addView(headerView);
    }

    private void addStatItem(LinearLayout container, String label, String value) {
        LinearLayout itemLayout = new LinearLayout(getContext());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 4, 0, 4);

        TextView labelView = new TextView(getContext());
        labelView.setText(label);
        labelView.setTextSize(13);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0, 
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView valueView = new TextView(getContext());
        valueView.setText(value);
        valueView.setTextSize(13);
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);
        valueView.setGravity(android.view.Gravity.END);

        itemLayout.addView(labelView);
        itemLayout.addView(valueView);
        container.addView(itemLayout);
    }

    private void addUserItem(LinearLayout container, String username, User user) {
        LinearLayout userLayout = new LinearLayout(getContext());
        userLayout.setOrientation(LinearLayout.VERTICAL);
        userLayout.setPadding(12, 8, 12, 8);
        userLayout.setBackgroundColor(0xFFFFFFFF);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 2, 0, 2);
        userLayout.setLayoutParams(params);

        TextView usernameView = new TextView(getContext());
        usernameView.setText("👤 " + username);
        usernameView.setTextSize(14);
        usernameView.setTypeface(null, android.graphics.Typeface.BOLD);
        userLayout.addView(usernameView);

        if (user != null) {
            if (user.contact != null && !user.contact.isEmpty()) {
                TextView contactView = new TextView(getContext());
                contactView.setText("📧 " + user.contact);
                contactView.setTextSize(12);
                userLayout.addView(contactView);
            }

            if (user.phone != null && !user.phone.isEmpty()) {
                TextView phoneView = new TextView(getContext());
                phoneView.setText("📱 " + user.phone);
                phoneView.setTextSize(12);
                userLayout.addView(phoneView);
            }

            TextView roleView = new TextView(getContext());
            roleView.setText("🔑 " + (user.role != null ? user.role : "Customer"));
            roleView.setTextSize(12);
            userLayout.addView(roleView);
        }

        container.addView(userLayout);
    }

    private String getTicketTypeDisplay(String type) {
        switch (type) {
            case "single": return "🎫 Vé lượt";
            case "daily": return "🌅 Vé ngày";
            case "monthly": return "📅 Vé tháng";
            case "quarterly": return "📆 Vé quý";
            case "yearly": return "🗓️ Vé năm";
            default: return "🎫 " + type;
        }
    }

    private String formatMonth(String month) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat output = new SimpleDateFormat("MM/yyyy");
            Date date = input.parse(month);
            return output.format(date);
        } catch (Exception e) {
            return month;
        }
    }

    private void showErrorMessage(String message) {
        TextView errorView = new TextView(getContext());
        errorView.setText("❌ " + message);
        errorView.setTextSize(14);
        errorView.setPadding(32, 32, 32, 32);
        errorView.setGravity(android.view.Gravity.CENTER);
        layoutContent.addView(errorView);
    }

    // CHART VISUALIZATION
    private void addTicketChart(LinearLayout container, Map<String, Integer> typeStats, int totalTickets) {
        if (typeStats.isEmpty() || totalTickets == 0) {
            addStatItem(container, "📊 Chưa có dữ liệu vé", "0");
            return;
        }
        
        // Tạo container cho chart
        LinearLayout chartContainer = new LinearLayout(getContext());
        chartContainer.setOrientation(LinearLayout.VERTICAL);
        chartContainer.setPadding(8, 8, 8, 8);
        chartContainer.setBackgroundColor(0xFFFFFFFF);
        
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        chartParams.setMargins(0, 8, 0, 8);
        chartContainer.setLayoutParams(chartParams);
        
        String[] orderedTypes = {"single", "daily", "monthly", "quarterly", "yearly"};
        
        for (String type : orderedTypes) {
            if (typeStats.containsKey(type)) {
                int count = typeStats.get(type);
                float percentage = totalTickets > 0 ? (float) count / totalTickets * 100 : 0;
                
                addTicketChartItem(chartContainer, getTicketTypeDisplay(type), count, percentage);
            }
        }
        
        container.addView(chartContainer);
    }

    private void addTicketChartItem(LinearLayout container, String label, int count, float percentage) {
        LinearLayout itemLayout = new LinearLayout(getContext());
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(12, 8, 12, 8);
        itemLayout.setBackgroundColor(0xFFF8F9FA);
        
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(0, 4, 0, 4);
        itemLayout.setLayoutParams(itemParams);

        // Label và giá trị
        LinearLayout headerLayout = new LinearLayout(getContext());
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setPadding(0, 0, 0, 4);

        TextView labelView = new TextView(getContext());
        labelView.setText(label);
        labelView.setTextSize(14);
        labelView.setTextColor(0xFF333333);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0, 
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView valueView = new TextView(getContext());
        valueView.setText(count + " vé (" + String.format("%.1f%%", percentage) + ")");
        valueView.setTextSize(13);
        valueView.setTextColor(getProgressBarColor(label));
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);
        valueView.setGravity(android.view.Gravity.END);

        headerLayout.addView(labelView);
        headerLayout.addView(valueView);

        // Progress bar chart với styling đẹp hơn
        ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 32);
        progressParams.setMargins(0, 4, 0, 0);
        progressBar.setLayoutParams(progressParams);
        progressBar.setMax(100);
        progressBar.setProgress((int) percentage);
        
        // Set màu cho progress bar với alpha cho hiệu ứng đẹp hơn
        int color = getProgressBarColor(label);
        progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        
        // Thêm background cho progress bar
        progressBar.setBackgroundColor(0xFFE0E0E0);

        itemLayout.addView(headerLayout);
        itemLayout.addView(progressBar);
        container.addView(itemLayout);
    }

    private int getProgressBarColor(String label) {
        if (label.contains("lượt")) return 0xFF4CAF50; // Green
        if (label.contains("ngày")) return 0xFF2196F3;   // Blue  
        if (label.contains("tháng")) return 0xFFFF9800;  // Orange
        if (label.contains("quý")) return 0xFF9C27B0;   // Purple
        if (label.contains("năm")) return 0xFFF44336;   // Red
        return 0xFF607D8B; // Default gray
    }
}
