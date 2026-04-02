# 🍽️ FOODIE - Restaurant Management System

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-yellow.svg)](https://www.thymeleaf.org/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blueviolet.svg)](https://stomp.github.io/)

## 📑 Tổng quan

**FOODIE** là hệ thống quản lý nhà hàng, được xây dựng nhằm số hóa toàn bộ quy trình từ khi khách đặt bàn, gọi món, chế biến, phục vụ đến thanh toán. Hệ thống hướng đến ba nhóm người dùng chính:

- **Quản lý (Admin)** – quản trị toàn bộ dữ liệu và giám sát hoạt động.
- **Nhân viên phục vụ (Waiter)** – quản lý bàn, xác nhận đơn, xử lý thanh toán.
- **Đầu bếp (Chef)** – theo dõi và cập nhật trạng thái chế biến món ăn.
- **Khách hàng (Customer)** – đặt bàn online, tự gọi món qua tablet, theo dõi trạng thái và yêu cầu thanh toán.

Hệ thống sử dụng **WebSocket** để cập nhật thời gian thực (real-time) giữa các thiết bị, đảm bảo sự phối hợp nhịp nhàng giữa bếp, phục vụ và khách hàng.

## 🔡 Tính năng chính

### 👨‍💼 Dành cho quản lý (Admin)
- Quản lý danh mục món ăn (thêm, sửa, xóa, tìm kiếm, phân trang)
- Quản lý món ăn (CRUD, upload ảnh, lọc theo danh mục, bật/tắt trạng thái bán)
- Quản lý bàn ăn (CRUD, lọc theo trạng thái/sức chứa, tìm kiếm theo mã bàn, xem thông tin bàn đã đặt)
- Quản lý chương trình giảm giá (tạo, sửa, xóa, kích hoạt theo thời gian)
- Quản lý nhân viên (CRUD, phân quyền, bật/tắt trạng thái đăng nhập)
- Xử lý yêu cầu đặt bàn (duyệt/từ chối, chọn bàn, gửi email xác nhận/từ chối)
- Dashboard thống kê (doanh thu theo ngày, top món bán chạy, số liệu tổng quan)
- Nhật ký hệ thống (audit log) – theo dõi mọi thay đổi dữ liệu

### 👨‍🍳 Dành cho Nnân viên phục vụ (Waiter)
- Sơ đồ bàn trực quan (trống/đang có khách/đã đặt trước)
- Mở bàn / check-in cho khách (tạo hóa đơn mới)
- Thêm, hủy món trực tiếp trên giao diện quản lý
- Áp dụng mã giảm giá, tính tổng tiền
- Xử lý thanh toán (tiền mặt/chuyển khoản) và in hóa đơn kèm mã QR
- Nhận thông báo real-time khi khách yêu cầu thanh toán hoặc bếp báo món đã xong

### 👨‍🍳 Dành cho đầu bếp (Chef)
- Xem danh sách món cần chế biến (theo thứ tự thời gian)
- Cập nhật trạng thái món: **PENDING** → **COOKING** → **SERVED**
- Hiển thị ghi chú đặc biệt của khách (ví dụ: "ít cay", "không hành")
- Thông báo real-time đến phục vụ và bàn khách khi món hoàn thành

### 👤 Dành cho khách hàng (Customer)
- Đặt bàn trực tuyến qua website (gửi yêu cầu, nhận email xác nhận)
- Đăng nhập bàn bằng mã bàn và mã hóa đơn
- Xem thực đơn điện tử (có hình ảnh, giá, mô tả, lọc theo danh mục)
- Thêm món vào giỏ, ghi chú, gửi yêu cầu xuống bếp
- Theo dõi trạng thái từng món (đang chờ, đang nấu, đã phục vụ)
- Yêu cầu thanh toán trực tiếp từ tablet

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|------------|
| **Backend** | Spring Boot 3, Spring MVC, Spring Data JPA, Spring Session JDBC |
| **Frontend** | Thymeleaf, Bootstrap 5, FontAwesome, JavaScript (AJAX) |
| **Realtime** | WebSocket (STOMP) với SockJS |
| **Database** | MySQL 8.0 |
| **Email** | JavaMailSender (SMTP) – hỗ trợ Gmail / Brevo |
| **Authentication** | Session-based (HttpSession) + Interceptor kiểm tra quyền |
| **Audit Log** | JSON serialization (Jackson) lưu old_value / new_value |
| **Build Tool** | Maven |

## 📁 Cấu trúc thư mục chính

```text
src/
└── main/
    ├── java/com/oose/restaurant_mis/
    │   ├── config/       # WebConfig, WebSocketConfig
    │   ├── controller/   # Admin, Waiter, Chef, Customer, Home controllers
    │   ├── entity/       # JPA entities
    │   ├── repository/   # JPA repositories
    │   ├── service/      # Business logic
    │   └── interceptor/  # AuthInterceptor
    └── resources/
        ├── application.properties
        └── templates/    # Thymeleaf HTML (admin, waiter, chef, customer, home)
uploads/                  # Thư mục chứa ảnh món ăn (tự động tạo)
```
## ⚙️ Hướng dẫn cài đặt và chạy

### Yêu cầu
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### Các bước

1. **Clone repository**
   ```bash
   git clone https://github.com/your-username/resto-mis.git
   cd resto-mis
2. **Tạo database MySQL**
   ```sql
   CREATE DATABASE restaurant_management_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
3. Cấu hình kết nối database
Sửa file src/main/resources/application.properties:
      ```bash
      spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_management_db?useSSL=false&serverTimezone=UTC
      spring.datasource.username=root
      spring.datasource.password=your_password
      ```

4. Cấu hình email (nếu cần gửi mail)
Thay đổi thông tin SMTP trong application.properties (có thể dùng Gmail hoặc Brevo).

5. Build và chạy
      ```bash
      mvn clean install
      mvn spring-boot:run
      ```

🌐 Truy cập ứng dụng

Trang chủ: http://localhost:8080

Đăng nhập admin: admin01 / 123456

Đăng nhập phục vụ: waiter01 / 123456

Đăng nhập bếp: chef01 / 123456

Đăng nhập trang tablet: http://localhost:8080/table/menu

Lưu ý: Bạn có thể thay đổi mật khẩu trong database sau khi chạy lần đầu.

🧪 Dữ liệu mẫu
Script SQL khởi tạo database kèm dữ liệu mẫu (bàn, danh mục, món ăn, tài khoản, chương trình giảm giá) được đính kèm trong file database.sql. Hệ thống sẽ tự động tạo cấu trúc bảng nếu chưa có (spring.jpa.hibernate.ddl-auto=none – khuyến nghị chạy script thủ công).

📬 WebSocket Endpoints
WebSocket endpoint: /ws-resto

Các topic đăng ký:
```bash

/topic/admin – thông báo đặt bàn mới

/topic/kitchen – thông báo món mới, hủy món

/topic/tables – cập nhật sơ đồ bàn, yêu cầu thanh toán

/topic/order/{orderId} – cập nhật trạng thái món, thay đổi đơn hàng

/topic/menu – thông báo thay đổi thực đơn
```

👥 Nhóm phát triển
```bash
Trần Hữu Lộc – Nghiệp vụ bếp + thanh toán + khách hàng (Frontend & WebSocket)

Nguyễn Khánh – Nghiệp vụ quản trị (Admin Frontend & Dashboard)

Ninh Anh Tú – Backend (API, Database, Security, WebSocket, Email)
```

📄 Giấy phép

Dự án được xây dựng với mục đích học tập và báo cáo. Vui lòng không sử dụng cho mục đích thương mại khi chưa có sự đồng ý của tác giả.

⭐ Nếu bạn thấy dự án hữu ích, hãy để lại một star trên GitHub nhé!

📧 Mọi thắc mắc xin liên hệ: ninhanhtu1704@gmail.com hoặc tranhuuloc05@gmail.com
