package com.unlockfps

import java.io.File
import kotlin.system.exitProcess

class UserService : IUserService.Stub() {
    override fun destroy() {
        exitProcess(0)
    }

    override fun saveFile(path: String, content: ByteArray): String {
        return try {
            val file = File(path)
            val parentFile = file.parentFile

            // 1. Kiểm tra và tạo thư mục nếu chưa có
            if (parentFile != null && !parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    return "Lỗi: Không thể tạo thư mục ${parentFile.absolutePath}"
                }
            }

            // 2. Kiểm tra quyền ghi của Shell đối với thư mục đích
            if (parentFile != null && !parentFile.canWrite()) {
                return "Lỗi: Shell không có quyền ghi vào ${parentFile.absolutePath}"
            }

            // 3. Thực hiện ghi file
            file.writeBytes(content)

            "OK" // Thành công
        } catch (e: SecurityException) {
            "Lỗi bảo mật: ${e.message}"
        } catch (e: Exception) {
            "Lỗi ghi file: ${e.localizedMessage ?: e.message}"
        }
    }

    override fun findLatestVersion(basePath: String): String? {
        val regex = Regex("""\d+\.\d+\.\d+.*""")
        return File(basePath).listFiles()?.filter { it.isDirectory && it.name.matches(regex) }
            ?.maxByOrNull { it.name }?.name
    }
}