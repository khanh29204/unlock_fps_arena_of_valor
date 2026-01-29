# AOV 120 FPS Unlocker (Shizuku)

![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

Một ứng dụng Android đơn giản sử dụng **Shizuku API** để can thiệp vào thư mục dữ liệu của trò chơi **Garena Liên Quân Mobile (com.garena.game.kgvn)** nhằm mở khóa tùy chọn 120 FPS (High Frame Rate).

## 🚀 Tính năng
- **Unlock 120 FPS:** Ghi đè file cấu hình whitelist để kích hoạt mức khung hình cao nhất.
- **Tự động dò tìm phiên bản:** Tự động tìm kiếm thư mục tài nguyên mới nhất.
- **Hỗ trợ Android 10+:**
    - Sử dụng **Shizuku User Service** cho Android 11 trở lên (Không cần Root).
    - Sử dụng **Legacy File API** cho Android 10.

## 📸 Ảnh chụp màn hình
![Main Screen](/screenshots/main_screen.png)

## 🛠 Yêu cầu hệ thống
1. **Thiết bị:** Android 10 trở lên.
2. **Shizuku:** Bạn phải cài đặt ứng dụng [Shizuku](https://shizuku.rikka.app/) và kích hoạt nó thông qua Wireless Debugging (LADB) hoặc Root.
3. **Game:** Đã cài đặt phiên bản Garena Liên Quân Mobile (VN).

## 📥 Cài đặt & Sử dụng
1. Tải về file `.apk` từ phần [Releases](https://github.com/khanh29204/AOV-FPS-Unlocker/releases).
2. Mở app **Shizuku**, đảm bảo trạng thái là "Shizuku is running".
3. Mở **AOV FPS Unlocker**, cấp quyền Shizuku khi được yêu cầu.
4. Nhấn nút **UNLOCK 120 FPS**.
5. Khởi động lại game và kiểm tra trong phần cài đặt khung hình.

## 🏗 Xây dựng ứng dụng (Dành cho Developer)
Sử dụng Android Studio để build project:
```bash
git clone https://github.com/khanh29204/AOV-FPS-Unlocker.git
cd AOV-FPS-Unlocker
./gradlew assembleDebug
```

**Cấu trúc quan trọng:**
- `src/main/aidl/`: Chứa file `IUserService.aidl` để giao tiếp với Shizuku.
- `src/main/assets/`: Chứa 2 file `.bytes` cần thiết để ghi đè vào game.
- `src/main/java/com/unlockfps/UserService.kt`: Chạy dưới quyền Shell để thực hiện thao tác file.

## ⚠️ Miễn trừ trách nhiệm (Disclaimer)
- **Rủi ro:** Việc can thiệp vào file hệ thống của game có thể vi phạm điều khoản dịch vụ của nhà phát hành. Chúng tôi không chịu trách nhiệm nếu tài khoản của bạn bị ảnh hưởng (khóa acc, lỗi tài nguyên...).
- **Mục đích:** Dự án này được tạo ra nhằm mục đích học tập và nghiên cứu về Shizuku API trên Android.

## 📜 Giấy phép
Dự án này được phát hành dưới giấy phép [MIT License](LICENSE).

---
**Đóng góp:** Nếu bạn gặp lỗi hoặc có ý tưởng mới, vui lòng mở một `Issue` hoặc gửi `Pull Request`!
**Người thực hiện:** [Phạm Ngọc Quốc Khánh] - [phamngocquockhanh2004@gmail.com]