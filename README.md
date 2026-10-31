# 🏨 Booking System - Spring Boot Backend

Dự án khởi tạo hệ thống quản lý đặt chỗ (Booking System) xây dựng trên nền tảng **Spring Boot 3.3.5** và **Java 17**, tuân thủ kiến trúc phân tầng chuẩn (Controller - Service - Repository - Entity - DTO).

---

## 🚀 Công nghệ & Thư viện sử dụng

- **Java**: 17 (hoặc 21)
- **Framework**: Spring Boot 3.3.5
- **Build Tool**: Maven (kèm Maven Wrapper `mvnw` / `mvnw.cmd`)
- **Các Starter & Dependencies**:
  - `spring-boot-starter-web`: Xây dựng RESTful API, nhúng Tomcat servlet container.
  - `spring-boot-starter-data-jpa`: Tương tác cơ sở dữ liệu qua Hibernate & Spring Data JPA.
  - `spring-boot-starter-validation`: Xác thực dữ liệu đầu vào (@Valid, @NotBlank, @Email, v.v.).
  - `lombok`: Tự động sinh getter/setter/builder/constructor.
  - `postgresql`: Driver kết nối PostgreSQL.
  - `mssql-jdbc`: Driver kết nối Microsoft SQL Server.
  - `spring-boot-starter-test`: Thư viện kiểm thử (JUnit 5, Mockito).

---

## 📁 Cấu trúc thư mục dự án

```text
c:\Users\admin\Desktop\web
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/com/booking/
│   │   │   ├── controller/
│   │   │   │   └── BookingController.java       # Tiếp nhận và xử lý REST endpoints
│   │   │   ├── service/
│   │   │   │   ├── BookingService.java           # Interface định nghĩa nghiệp vụ
│   │   │   │   └── impl/
│   │   │   │       └── BookingServiceImpl.java   # Cài đặt nghiệp vụ đặt chỗ
│   │   │   ├── repository/
│   │   │   │   └── BookingRepository.java        # Tầng truy xuất dữ liệu (Spring Data JPA)
│   │   │   ├── entity/
│   │   │   │   ├── Booking.java                  # JPA Entity ánh xạ bảng `bookings`
│   │   │   │   └── BookingStatus.java            # Enum trạng thái (PENDING, CONFIRMED, ...)
│   │   │   ├── dto/
│   │   │   │   ├── BookingRequestDTO.java        # DTO nhận dữ liệu gửi lên (có Validation)
│   │   │   │   └── BookingResponseDTO.java       # DTO trả dữ liệu về client
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java# Ngoại lệ tùy chỉnh khi không tìm thấy tài nguyên
│   │   │   │   ├── ErrorResponse.java            # Khuôn mẫu phản hồi lỗi chuẩn REST
│   │   │   │   └── GlobalExceptionHandler.java   # Bắt và chuẩn hóa lỗi toàn hệ thống (@RestControllerAdvice)
│   │   │   └── BookingSystemApplication.java     # Main Application entry point
│   │   └── resources/
│   │       ├── application.properties            # Cấu hình chính (chọn active profile)
│   │       ├── application-postgres.properties   # Cấu hình kết nối PostgreSQL
│   │       └── application-sqlserver.properties  # Cấu hình kết nối SQL Server
│   └── test/
│       └── java/com/booking/
│           └── BookingSystemApplicationTests.java
├── .gitignore                                    # File bỏ qua cho Git (Maven, IntelliJ, VS Code...)
├── mvnw                                          # Maven wrapper cho Linux/macOS
├── mvnw.cmd                                      # Maven wrapper cho Windows
├── pom.xml                                       # File cấu hình Maven & Dependencies
└── README.md                                     # Tài liệu hướng dẫn sử dụng
```

---

## 🗄️ Cấu hình Cơ sở dữ liệu

Dự án đã chuẩn bị sẵn 2 profile cấu hình:

### 1. Sử dụng PostgreSQL (Mặc định)
Tại file `src/main/resources/application.properties`:
```properties
spring.profiles.active=postgres
```
Chỉnh sửa thông số kết nối tại `src/main/resources/application-postgres.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/booking_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 2. Sử dụng Microsoft SQL Server
Tại file `src/main/resources/application.properties`:
```properties
spring.profiles.active=sqlserver
```
Chỉnh sửa thông số kết nối tại `src/main/resources/application-sqlserver.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=booking_db;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrong@Passw0rd
```

---

## 🔌 Danh sách API Endpoints (`/api/bookings`)

| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `POST` | `/api/bookings` | Tạo mới một đơn đặt chỗ (kèm validate dữ liệu) |
| `GET` | `/api/bookings` | Lấy danh sách tất cả các đơn đặt chỗ |
| `GET` | `/api/bookings/{id}` | Lấy chi tiết đơn đặt chỗ theo ID |
| `PUT` | `/api/bookings/{id}` | Cập nhật thông tin đơn đặt chỗ |
| `PATCH`| `/api/bookings/{id}/status?status=CONFIRMED` | Cập nhật trạng thái (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`) |
| `DELETE`| `/api/bookings/{id}` | Xóa đơn đặt chỗ |

### Ví dụ Payload tạo mới (`POST /api/bookings`):
```json
{
  "customerName": "Nguyen Van A",
  "customerEmail": "nguyenvana@example.com",
  "customerPhone": "0987654321",
  "serviceName": "VIP Meeting Room A",
  "startTime": "2026-11-05T09:00:00",
  "endTime": "2026-11-05T11:00:00",
  "totalPrice": 150.00,
  "note": "Yêu cầu máy chiếu và nước uống"
}
```

---

## 🛠️ Hướng dẫn Chạy dự án

### Cách 1: Sử dụng Maven Wrapper (khuyến nghị trên Windows)
```powershell
.\mvnw.cmd spring-boot:run
```

### Cách 2: Sử dụng Maven đã cài đặt
```powershell
mvn clean install
mvn spring-boot:run
```

### Cách 3: Mở trong IDE
- Mở thư mục dự án bằng **IntelliJ IDEA**, **Eclipse** hoặc **VS Code**.
- Chọn file `pom.xml` để IDE nhận diện dự án Maven.
- Chạy phương thức `main` trong file `BookingSystemApplication.java`.

