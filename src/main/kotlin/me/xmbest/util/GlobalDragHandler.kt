package me.xmbest.util

import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.awtTransferable
import me.xmbest.model.DialogState
import me.xmbest.screen.file.extractFilesFromEvent
import java.awt.datatransfer.DataFlavor
import java.io.File

object GlobalDragHandler {
    @OptIn(ExperimentalComposeUiApi::class)
    fun shouldStartDragAndDrop(event: DragAndDropEvent): Boolean {
        return try {
            val transferable = event.awtTransferable
            transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                    transferable.isDataFlavorSupported(DataFlavor.stringFlavor)
        } catch (_: Exception) {
            false
        }
    }

    fun handleDrop(
        event: DragAndDropEvent,
        dialogState: MutableState<DialogState>,
        onInstall: (String) -> Unit
    ): Boolean {
        val files = extractFilesFromEvent(event)
        if (files.isEmpty()) return false

        val apkFiles = files.filter { it.endsWith(".apk", ignoreCase = true) }
        if (apkFiles.isNotEmpty()) {
            showApkDialog(dialogState, apkFiles[0], onInstall)
            return true
        }
        return false
    }

    private fun showApkDialog(
        dialogState: MutableState<DialogState>,
        apkPath: String,
        onInstall: (String) -> Unit
    ) {
        DialogUtil.showConfirm(
            dialogState = dialogState,
            title = DialogUtil.strings.get("dialog.title.tip"),
            message = "是否安装 ${File(apkPath).name}",
            confirmText = DialogUtil.strings.get("button.install"),
            onConfirm = { onInstall(apkPath) },
            onCancel = {}
        )
    }
}
