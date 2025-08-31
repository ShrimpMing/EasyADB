package com.xmbest.ddmlib

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * 写入shell命令到文件
 */
fun writeShell(file: File, content: String) {
    val output = BufferedOutputStream(FileOutputStream(file))
    output.write((content).toByteArray())
    output.flush()
    output.close()
}