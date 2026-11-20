package com.andy.assistant

import android.content.Context
import android.net.Uri
import java.io.File

object LocalModelStore {
    fun import(context: Context, uri: Uri): File {
        val dir = File(context.filesDir, "models").apply { mkdirs() }
        val name = "andy-model.gguf"
        val target = File(dir, name)
        val temp = File(dir, "$name.part")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open selected model file" }.use { source ->
                temp.outputStream().use { output -> source.copyTo(output) }
            }
        }
        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
        return target
    }

    fun installed(context: Context): File? =
        File(context.filesDir, "models/andy-model.gguf").takeIf { it.exists() && it.length() > 0L }
}
