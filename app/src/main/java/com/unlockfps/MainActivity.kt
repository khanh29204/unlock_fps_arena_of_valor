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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku
import java.io.File

class MainActivity : AppCompatActivity() {

    private var userService: IUserService? = null
    private val SHIZUKU_CODE = 1000
    private val BASE_PATH = "/storage/emulated/0/Android/data/com.garena.game.kgvn/files/Resources/"

    private lateinit var txtShizukuStatus: TextView
    private lateinit var txtGameStatus: TextView
    private lateinit var btnUnlock: Button

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            userService = IUserService.Stub.asInterface(binder)
            runOnUiThread {
                updateGameVersionUI()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
        }
    }

    private val userServiceArgs by lazy {
        Shizuku.UserServiceArgs(ComponentName(packageName, UserService::class.java.name))
            .processNameSuffix("unlock_fps").debuggable(BuildConfig.DEBUG).daemon(false)
    }

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == SHIZUKU_CODE && grantResult == PackageManager.PERMISSION_GRANTED) {
                bindMyService()
            }
            updateShizukuStatusUI()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtShizukuStatus = findViewById(R.id.txtShizukuStatus)
        txtGameStatus = findViewById(R.id.txtGameStatus)
        btnUnlock = findViewById(R.id.btnUnlock)

        Shizuku.addRequestPermissionResultListener(permissionListener)
        updateShizukuStatusUI()

        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            bindMyService()
        }

        btnUnlock.setOnClickListener {
            checkPermissionAndAction()
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
            } else {
                txtGameStatus.text = getString(R.string.version_game_not_found)
            }
        } catch (e: Exception) {
            Log.e("UI", "Error updating version: ${e.message}")
        }
    }

    private fun checkPermissionAndAction() {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(this, "Shizuku chưa khởi chạy!", Toast.LENGTH_SHORT).show()
            return
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            if (userService == null) {
                bindMyService()
                Toast.makeText(this, "Đang kết nối Service...", Toast.LENGTH_SHORT).show()
            } else {
                unlockFps()
            }
        } else {
            Shizuku.requestPermission(SHIZUKU_CODE)
        }
    }

    private fun bindMyService() {
        try {
            Shizuku.bindUserService(userServiceArgs, connection)
        } catch (e: Exception) {
            Log.e("UnlockFPS", "Bind error: ${e.message}")
        }
    }

    private fun unlockFps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val service = userService ?: return
            try {
                val versionFolder = service.findLatestVersion(BASE_PATH) ?: return
                val targetPath = "$BASE_PATH$versionFolder/Databin/Client/Text/"

                val data1 = assets.open("60FPSWhiteList.bytes").readBytes()
                val data2 = assets.open("VeryHighFrameModeBlackList.bytes").readBytes()

                val res1 = service.saveFile("${targetPath}60FPSWhiteList.bytes", data1)
                val res2 = service.saveFile("${targetPath}VeryHighFrameModeBlackList.bytes", data2)

                if (res1 == "OK" && res2 == "OK") {
                    Toast.makeText(this, "Unlock thành công!", Toast.LENGTH_SHORT).show()
                } else {
                    // Hiển thị chi tiết lỗi nếu thất bại
                    val errorMsg = if (res1 != "OK") res1 else res2
                    Log.e("UnlockFPS", "Thất bại: $errorMsg")
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("Unlock", e.message.toString())
            }
        } else {
            executeLegacyUnlock()
        }
    }

    private fun executeLegacyUnlock() {
        try {
            val baseDir = File(BASE_PATH)
            val versionFolder = baseDir.listFiles()
                ?.filter { it.isDirectory && it.name.matches(Regex("""\d+\.\d+\.\d+.*""")) }
                ?.maxByOrNull { it.name }?.name ?: return

            val targetDir = File("$BASE_PATH$versionFolder/Databin/Client/Text/")
            if (!targetDir.exists()) targetDir.mkdirs()

            assets.open("60FPSWhiteList.bytes").use { input ->
                File(targetDir, "60FPSWhiteList.bytes").outputStream().use { input.copyTo(it) }
            }
            assets.open("VeryHighFrameModeBlackList.bytes").use { input ->
                File(targetDir, "VeryHighFrameModeBlackList.bytes").outputStream()
                    .use { input.copyTo(it) }
            }

            Toast.makeText(this, "Unlock thành công (Legacy)!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
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