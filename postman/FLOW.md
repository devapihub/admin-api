# Testing Flow

## Setup

1. Khởi động app: `mvn spring-boot:run`
2. Mở `http-client.env.json`, chọn environment `local` trong IntelliJ

---

## Flow test đầy đủ

### Bước 1 — Đăng ký & đăng nhập admin
```
auth.http [2] → đăng ký user "admin"
auth.http [4] → đăng nhập "admin" → copy token → dán vào http-client.env.json > "token"
```
> Vào MongoDB, cập nhật roles của "admin" thành ROLE_ADMIN thủ công (lần đầu tiên).

### Bước 2 — Tạo permissions
```
permissions.http [2] → tạo READ_USER  → copy id
permissions.http [3] → tạo WRITE_USER → copy id
permissions.http [4] → tạo DELETE_USER
permissions.http [5] → tạo READ_ORDER
```

### Bước 3 — Tạo role & gán permissions
```
roles.http [1]  → xem roles hiện có, copy roleId của ROLE_MANAGER (hoặc tạo mới [2])
roles.http [4]  → gán READ_USER + WRITE_USER vào ROLE_MANAGER
```

### Bước 4 — Gán role cho user
```
users.http [1]  → lấy danh sách users, copy userId của "alice"
users.http [2]  → gán ROLE_MANAGER cho "alice"
```

### Bước 5 — Xác nhận
```
auth.http [3]   → đăng nhập lại "alice" → kiểm tra response có roles & permissions
general.http [2]→ xem profile "alice" → thấy permissions từ role
```

---

## Dùng `@PreAuthorize` với permission trong code

```java
@PreAuthorize("hasAuthority('READ_USER')")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('WRITE_USER')")
```
