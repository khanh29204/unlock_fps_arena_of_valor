package com.unlockfps

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import rikka.shizuku.Shizuku
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var userService: IUserService? = null
    private val SHIZUKU_CODE = 1000
    private val BASE_PATH = "/storage/emulated/0/Android/data/com.garena.game.kgvn/files/Resources/"

    private lateinit var txtShizukuStatus: TextView
    private lateinit var txtGameStatus: TextView
    private lateinit var btnUnlock: Button
    private lateinit var inputLog: TextInputEditText

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            userService = IUserService.Stub.asInterface(binder)
            runOnUiThread {
                addLog("Service kết nối thành công.")
                updateGameVersionUI()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
            addLog("Service đã ngắt kết nối.")
        }
    }

    private val userServiceArgs by lazy {
        Shizuku.UserServiceArgs(ComponentName(packageName, UserService::class.java.name))
            .processNameSuffix("unlock_fps").debuggable(BuildConfig.DEBUG).daemon(false)
    }

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == SHIZUKU_CODE && grantResult == PackageManager.PERMISSION_GRANTED) {
                addLog("Đã cấp quyền Shizuku.")
                bindMyService()
            } else {
                addLog("Quyền Shizuku bị từ chối.")
            }
            updateShizukuStatusUI()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtShizukuStatus = findViewById(R.id.txtShizukuStatus)
        txtGameStatus = findViewById(R.id.txtGameStatus)
        btnUnlock = findViewById(R.id.btnUnlock)
        inputLog = findViewById(R.id.input_log)

        inputLog.keyListener = null
        // Đảm bảo log rỗng khi khởi động
        inputLog.setText("")

        Shizuku.addRequestPermissionResultListener(permissionListener)
        updateShizukuStatusUI()

        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            bindMyService()
        }

        btnUnlock.setOnClickListener {
            checkPermissionAndAction()
        }
    }

    // Hàm tiện ích để ghi log vào EditText
    private fun addLog(message: String) {
        runOnUiThread {
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logMessage = "[$time] $message\n"
            inputLog.append(logMessage)

            // Tự động cuộn xuống dòng cuối cùng nếu text quá dài
            inputLog.setSelection(inputLog.text?.length ?: 0)
        }
    }

    private fun updateShizukuStatusUI() {
        if (!Shizuku.pingBinder()) {
            txtShizukuStatus.text = getString(R.string.shizuku_not_ready)
            txtShizukuStatus.setTextColor(getColor(R.color.text_secondary))
            btnUnlock.text = getString(R.string.unlock_60_120_fps)
            return
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            txtShizukuStatus.text = getString(R.string.shizuku_ready)
            txtShizukuStatus.setTextColor(getColor(R.color.success_green))
            btnUnlock.text = getString(R.string.unlock_60_120_fps)
        } else {
            txtShizukuStatus.text = getString(R.string.shizuku_permission_not_granted)
            txtShizukuStatus.setTextColor(getColor(R.color.text_secondary))
            btnUnlock.text = getString(R.string.btn_grant_permission)
        }
    }

    private fun updateGameVersionUI() {
        try {
            val version = userService?.findLatestVersion(BASE_PATH)
            if (version != null) {
                txtGameStatus.text = getString(R.string.version_game, version)
                txtGameStatus.setTextColor(getColor(R.color.success_green))
                addLog("Tìm thấy phiên bản game: $version")
            } else {
                txtGameStatus.text = getString(R.string.version_game_not_found)
                addLog("Không tìm thấy thư mục phiên bản game.")
            }
        } catch (e: Exception) {
            Log.e("UI", "Error updating version: ${e.message}")
            addLog("Lỗi kiểm tra version: ${e.message}")
        }
    }

    private fun checkPermissionAndAction() {
        if (!Shizuku.pingBinder()) {
            addLog("Shizuku chưa khởi chạy!")
            return
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            if (userService == null) {
                bindMyService()
                addLog("Đang kết nối Service...")
            } else {
                unlockFps()
            }
        } else {
            addLog("Đang yêu cầu cấp quyền...")
            Shizuku.requestPermission(SHIZUKU_CODE)
        }
    }

    private fun bindMyService() {
        try {
            Shizuku.bindUserService(userServiceArgs, connection)
        } catch (e: Exception) {
            Log.e("UnlockFPS", "Bind error: ${e.message}")
            addLog("Lỗi Bind Service: ${e.message}")
        }
    }

    private fun unlockFps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val service = userService ?: return
            try {
                addLog("Bắt đầu xử lý (Mode: Root/Shizuku)...")
                val versionFolder = service.findLatestVersion(BASE_PATH)
                if (versionFolder == null) {
                    addLog("Lỗi: Không tìm thấy thư mục version!")
                    return
                }

                val targetPath = "$BASE_PATH$versionFolder/Databin/Client/Text/"
                addLog("Target: $targetPath")

                val data1 = assets.open("60FPSWhiteList.bytes").readBytes()
                val data2 = assets.open("VeryHighFrameModeBlackList.bytes").readBytes()

                val res1 = service.saveFile("${targetPath}60FPSWhiteList.bytes", data1)
                val res2 = service.saveFile("${targetPath}VeryHighFrameModeBlackList.bytes", data2)

                if (res1 == "OK" && res2 == "OK") {
                    addLog("Unlock thành công!")
                } else {
                    // Hiển thị chi tiết lỗi nếu thất bại
                    val errorMsg = if (res1 != "OK") res1 else res2
                    Log.e("UnlockFPS", "Thất bại: $errorMsg")
                    addLog("Thất bại: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("Unlock", e.message.toString())
                addLog("Exception: ${e.message}")
            }
        } else {
            executeLegacyUnlock()
        }
    }

    private fun executeLegacyUnlock() {
        try {
            addLog("Bắt đầu xử lý (Mode: Legacy)...")
            val baseDir = File(BASE_PATH)

            if (!baseDir.exists()) {
                addLog("Không tìm thấy đường dẫn gốc.")
                return
            }

            val versionFolder = baseDir.listFiles()
                ?.filter { it.isDirectory && it.name.matches(Regex("""\d+\.\d+\.\d+.*""")) }
                ?.maxByOrNull { it.name }?.name

            if (versionFolder == null) {
                addLog("Không tìm thấy folder version hợp lệ.")
                return
            }

            val targetDir = File("$BASE_PATH$versionFolder/Databin/Client/Text/")
            if (!targetDir.exists()) targetDir.mkdirs()

            assets.open("60FPSWhiteList.bytes").use { input ->
                File(targetDir, "60FPSWhiteList.bytes").outputStream().use { input.copyTo(it) }
            }
            assets.open("VeryHighFrameModeBlackList.bytes").use { input ->
                File(targetDir, "VeryHighFrameModeBlackList.bytes").outputStream()
                    .use { input.copyTo(it) }
            }

            addLog("Unlock thành công (Legacy)!")
        } catch (e: Exception) {
            addLog("Lỗi: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        try {
            Shizuku.unbindUserService(userServiceArgs, connection, true)
        } catch (_: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        updateShizukuStatusUI()
    }
}