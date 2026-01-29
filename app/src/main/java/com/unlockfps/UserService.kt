package com.unlockfps

import java.io.File
import kotlin.system.exitProcess

class UserService : IUserService.Stub() {
    override fun destroy() {
        exitProcess(0)
    }

    override fun saveFile(path: String, content: ByteArray): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeBytes(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun findLatestVersion(basePath: String): String? {
        val regex = Regex("""\d+\.\d+\.\d+.*""")
        return File(basePath).listFiles()
            ?.filter { it.isDirectory && it.name.matches(regex) }
            ?.maxByOrNull { it.name }
            ?.name
    }
}