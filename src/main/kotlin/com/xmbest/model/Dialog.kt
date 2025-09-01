package com.xmbest.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class DialogType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS,
    CUSTOM
}

data class DialogState(
    val isVisible: Boolean = false,
    val type: DialogType = DialogType.INFO,
    val title: String = "",
    val message: String = "",
    val confirmText: String = "确定",
    val cancelText: String = "取消",
    val icon: ImageVector? = null,
    val onConfirm: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null,
    val customContent: (@Composable () -> Unit)? = null
)