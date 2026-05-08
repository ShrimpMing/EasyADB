package me.xmbest.util

import me.xmbest.appStorageAbsolutePath
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date

object ErrorLogger {

    private val errorDir: File
        get() = File(appStorageAbsolutePath, "error").also { it.mkdirs() }

    private val logFile: File
        get() {
            val date = SimpleDateFormat("yyyy-MM-dd").format(Date())
            return File(errorDir, "$date.txt")
        }

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    fun log(throwable: Throwable) {
        val sw = StringWriter()
        PrintWriter(sw).use { pw ->
            pw.println("${timeFormat.format(Date())} [${Thread.currentThread().name}] ${throwable.message}")
            throwable.printStackTrace(pw)
            pw.println()
        }
        runCatching {
            logFile.appendText(sw.toString())
        }
    }

    fun log(tag: String, message: String, throwable: Throwable? = null) {
        val entry = buildString {
            append("${timeFormat.format(Date())} [${Thread.currentThread().name}] ")
            append("[$tag] $message")
            if (throwable != null) {
                appendLine()
                val sw = StringWriter()
                PrintWriter(sw).use { throwable.printStackTrace(it) }
                append(sw)
            }
            appendLine()
        }
        runCatching {
            logFile.appendText(entry)
        }
    }
}