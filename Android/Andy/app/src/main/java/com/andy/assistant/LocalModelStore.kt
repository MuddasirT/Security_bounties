package com.andy.assistant

import android.content.Context
import android.net.Uri
import java.io.File

object LocalModelStore {
    fun import(context: Context, uri: Uri): File {
        val dir = File(context.filesDir, "models").apply { mkdirs() }
        val target = File(dir, "andy-model.gguf")
        val temp = File(dir, "andy-model.gguf.part")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open selected model file" }.use { source ->
                temp.outputStream().use { output -> source.copyTo(output) }
            }
        }
        require(temp.length() > 1024L * 1024L) { "Selected file is too small to be a GGUF model." }
        val header = ByteArray(4)
        temp.inputStream().use { stream ->
            require(stream.read(header) == 4 && header.contentEquals(byteArrayOf(0x47, 0x47, 0x55, 0x46))) {
                "This file does not have a GGUF header. Select a .gguf model."
            }
        }
        if (!temp.renameTo(target)) { temp.copyTo(target, overwrite = true); temp.delete() }
        return target
    }

    fun installed(context: Context): File? =
        File(context.filesDir, "models/andy-model.gguf").takeIf { it.exists() && it.length() > 0L }
}
