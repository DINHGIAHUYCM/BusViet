# BusViet - Tính năng Quản lý & Thống kê vé 

## 🎯 Tính năng mới đã implement

### 1. **Admin - Xem danh sách User đã mua vé hợp lệ trong tháng**
- **Truy cập**: Menu "Quản lý vé" (chỉ hiển thị với tài khoản Admin)
- **Chức năng**: 
  - Chọn tháng cụ thể để xem thống kê
  - Hiển thị danh sách user có vé còn hiệu lực trong tháng đó
  - Thông tin chi tiết: Username, Email, Phone, Role

### 2. **Thống kê theo tháng**
- **Dữ liệu hiển thị**:
  - 👥 Số người dùng có vé hợp lệ trong tháng
  - 🛒 Tổng lượt mua vé trong tháng  
  - 🎫 Tổng số vé bán ra
  - 💰 Doanh thu tháng
  - 📊 Phân loại vé (ngày/tháng/quý/năm)

### 3. **Thống kê tổng quan**
- **Hiển thị**:
  - Tổng số người dùng đã mua vé
  - Tổng số vé đã bán
  - Tổng doanh thu
  - Phân loại theo loại vé
  - Thống kê theo năm

## 🚀 Hướng dẫn sử dụng

### Bước 1: Đăng nhập Admin
```
Username: admin1
Password: [tạo mật khẩu]
Role: Admin ✅
```

### Bước 2: Tạo dữ liệu test (Optional)
1. Chọn tab **"Test Tools"** (chỉ hiển thị với Admin)
2. Click **"TẠO DỮ LIỆU TEST"** 
3. Hệ thống sẽ tạo:
   - 5 tuyến xe mẫu
   - 60-120 giao dịch mua vé
   - Dữ liệu trải đều qua 6 tháng gần nhất

### Bước 3: Xem thống kê
1. Chọn tab **"Quản lý vé"**
2. Sử dụng dropdown chọn tháng để xem chi tiết
3. Scroll xuống để xem danh sách user có vé hợp lệ

### Bước 4: Mua vé (Test thêm data)
1. Chọn tab **"Mua vé"**
2. Chọn tuyến xe, loại vé, số lượng
3. Click **"MUA VÉ"**

## 🎨 Theme & UI

- **Màu chủ đạo**: `#D30A00` (đỏ)
- **Màu phụ**: 
  - `#A30700` (đỏ đậm)
  - `#FFE5E5` (đỏ nhạt)
- **Design**: Material Design với các card layout
- **Icons**: Emoji cho dễ nhận biết

## 📱 Cấu trúc Database

### Purchase Model
```java
{
  "username": "user1",
  "routeCode": "01", 
  "ticketType": "monthly",
  "ticketCount": 2,
  "purchaseDate": 1735123200000,
  "expireDate": 1737801600000,
  "totalPrice": 900000,
  "isValid": true
}
```

### Ticket Types
- `daily`: Vé ngày (1 ngày)
- `monthly`: Vé tháng (30 ngày) 
- `quarterly`: Vé quý (90 ngày)
- `yearly`: Vé năm (365 ngày)

## 🔧 Technical Implementation

### Models Created
- `Purchase.java` - Model cho giao dịch mua vé
- `TicketStatistics.java` - Model cho thống kê
- `AdminTestFragment.java` - Tool tạo data test

### Fragments Updated  
- `TicketManagerFragment.java` - Giao diện admin thống kê
- `BuyTicketFragment.java` - Giao diện mua vé

### Layouts Created/Updated
- `fragment_ticket_manager.xml` - Layout responsive với ScrollView
- `fragment_buy_ticket.xml` - Form mua vé user-friendly
- `fragment_admin_test.xml` - Tool admin

## 📊 Data Flow

1. **User mua vé** → `BuyTicketFragment` → Save `Purchase` vào Firebase
2. **Admin xem thống kê** → `TicketManagerFragment` → Query `Purchase` data
3. **Filter theo tháng** → Logic filter trong `Purchase.getPurchaseMonth()`
4. **Check vé hợp lệ** → `Purchase.isValidInCurrentMonth()`

## 🎉 Features Completed

✅ Admin có thể xem danh sách user có vé hợp lệ theo tháng  
✅ Thống kê chi tiết theo tháng/quý/năm  
✅ Thống kê theo loại vé (ngày/tháng/quý/năm)  
✅ Giao diện đẹp với màu chủ đạo #D30A00  
✅ Tool tạo data test cho admin  
✅ Form mua vé hoàn chỉnh cho user  

## 🔮 Potential Enhancements

- 📈 Biểu đồ visualization cho thống kê
- 📤 Export data ra Excel/PDF  
- 🔔 Push notification khi vé sắp hết hạn
- 💳 Payment gateway integration
- 📍 GPS tracking cho bus real-time

---

**Developed with ❤️ using Android Java + Firebase**
