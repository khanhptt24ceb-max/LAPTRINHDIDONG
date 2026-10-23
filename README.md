# ECOMER — Ứng dụng Quản lý Sản phẩm (Kotlin + Firebase Cloud Firestore)

Ứng dụng Android quản lý sản phẩm theo yêu cầu đề thi ECOMER với đầy đủ 3 phần A, B, C (10/10 điểm).

## 🛠 Công nghệ sử dụng
- **Ngôn ngữ:** Kotlin (ViewBinding, Lambda Callbacks, LruCache, Safe Deserialization)
- **IDE:** Android Studio
- **Cơ sở dữ liệu:** Firebase Cloud Firestore (`ecomer-7b1dc`)
- **Xác thực & Phân quyền:** Firebase Authentication (Email/Password) + Firestore `users` role
- **Min SDK:** 24 (Android 7.0) | **Compile/Target SDK:** 34

---

## 📋 Chức năng đã hoàn thành (10.0 / 10.0 điểm)

### Phần A — CRUD sản phẩm trên Cloud Firestore (8.5 điểm)
- ✅ **A1:** Collection `products` lưu đúng các trường yêu cầu (`tenSanPham`, `loaiSanPham`, `gia`) — tuyệt đối không ghi trường `id` hay trường thừa vào document.
- ✅ **A2 (Thêm):** Nhập dữ liệu từ form → Validate → Thêm document mới vào Firestore.
- ✅ **A3 (Hiển thị):** Lắng nghe dữ liệu Realtime (`addSnapshotListener`) → Hiển thị lên `RecyclerView` đúng bố cục ảnh mẫu.
- ✅ **A4 (Sửa):** Bấm nút ✏️ (viền vàng) → Đổ dữ liệu ngược lên form → Cập nhật document trên Firestore (kèm nút `HỦY` chế độ sửa).
- ✅ **A5 (Xóa):** Bấm nút 🗑️ (viền đỏ) → Hộp thoại xác nhận → Xóa document khỏi Firestore.

### Phần B — Bổ sung trường Image (+0.5 điểm)
- ✅ **B1:** Bổ sung trường `image` (chuỗi Base64 đã nén tối ưu JPEG < 360px) vào document `products`.
- ✅ **B2:** Bấm biểu tượng 📁 mở bộ chọn ảnh từ máy → Hiển thị tên file ảnh + ảnh xem trước (preview).
- ✅ **B3:** Hiển thị hình ảnh thumbnail bo góc bên trái mỗi sản phẩm (có `LruCache` tăng tốc cuộn).

### Phần C — Đăng nhập, Đăng xuất, Phân quyền User (+1.0 điểm)
- ✅ **C1 (Đăng nhập / Đăng ký):** Màn hình Login & Register với Firebase Authentication (hỗ trợ chọn vai trò và điền nhanh tài khoản Demo).
- ✅ **C2 (Đăng xuất):** Nút Đăng xuất trên thanh trạng thái quay về màn hình Login.
- ✅ **C3 (Phân quyền):**
  - **Admin (`role = "admin"`):** Toàn quyền Xem, Thêm, Sửa, Xóa sản phẩm.
  - **User (`role = "user"`):** Chỉ xem danh sách sản phẩm (tự động ẩn Form nhập và ẩn nút Sửa/Xóa).

---

## 🔑 Tài khoản Demo đã tạo sẵn trên Firebase

| Vai trò | Email | Mật khẩu | Quyền hạn |
|---------|-------|----------|-----------|
| **Admin** | `admin@ecomer.com` | `123456` | Toàn quyền Thêm / Sửa / Xóa / Xem |
| **User** | `user@ecomer.com` | `123456` | Chỉ xem danh sách sản phẩm |

*(Trên màn hình Đăng nhập có sẵn 2 nút **👤 Admin (CRUD)** và **👥 User (Chỉ xem)** để điền nhanh trong 1 chạm khi quay video demo).*

---

## 📂 Kiến trúc thư mục Project

```text
ECOMER/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json               ← Cấu hình Firebase (ecomer-7b1dc)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/ecomer/
│       │   │   ├── Product.kt             ← Model sản phẩm + toFirestoreMap() chuẩn 4 trường
│       │   │   ├── ProductAdapter.kt      ← Adapter RecyclerView + LruCache Base64
│       │   │   ├── MainActivity.kt        ← Màn hình chính: CRUD + Chọn ảnh + Phân quyền
│       │   │   ├── LoginActivity.kt       ← Màn hình Đăng nhập + Quick Demo fill
│       │   │   └── RegisterActivity.kt    ← Màn hình Đăng ký + Chọn Role
│       │   └── res/
│       │       ├── drawable/              ← Borders, Icons (edit, delete, folder, logout)
│       │       ├── layout/                ← activity_main, item_product, activity_login, activity_register
│       │       ├── mipmap-anydpi-v26/     ← Adaptive Launcher Icons
│       │       └── values/                ← colors.xml, strings.xml, themes.xml
│       └── test/java/com/example/ecomer/
│           └── ProductTest.kt             ← Unit Test kiểm tra cấu trúc trường Firestore
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── csdl.json                              ← File xuất CSDL thực tế từ Firestore để nộp bài
└── README.md
```

---

## 🚀 Hướng dẫn mở Project trên Windows
Để tránh cảnh báo đường dẫn tiếng Việt (`giữa kỳ`) của bộ công cụ Android SDK trên Windows, một **Directory Junction** chuẩn ASCII đã được tạo sẵn tại:
- Đường dẫn ASCII khuyên dùng khi mở bằng Android Studio: `C:\Users\Dell\Downloads\ECOMER`
- Đường dẫn gốc (đồng bộ 100% thời gian thực): `C:\Users\Dell\Downloads\giữa kỳ\ECOMER`
