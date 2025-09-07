package me.xmbest.screen.file

import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.xmbest.FILE_SPLIT
import me.xmbest.LocalSnackbarHostState
import me.xmbest.ddmlib.ClipboardUtil
import me.xmbest.ddmlib.DeviceManager
import me.xmbest.screen.empty.EmptyScreen
import me.xmbest.theme.CardShape
import java.awt.datatransfer.DataFlavor

@Composable
fun FileScreen(viewModel: FileViewModel = viewModel()) {
    val device = DeviceManager.device.collectAsState().value
    if (device == null) {
        EmptyScreen()
    } else {
        FileScreenContent(viewModel)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FileScreenContent(viewModel: FileViewModel) {
    val scrollState = rememberScrollState()
    val uiState = viewModel.uiState.collectAsState().value

    val requester = FocusRequester()

    SideEffect {
        requester.requestFocus()
    }

    FileScreenSideEffects(viewModel, uiState)
    Box(
        modifier = Modifier.fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = ::shouldStartDragAndDrop, target = rememberDragAndDropTarget(viewModel)
            )
            .onKeyEvent(
                { setKeyEvent(viewModel, uiState, it) }
            )
            .focusRequester(requester).focusable()
    ) {
        FileList(
            viewModel = viewModel, uiState = uiState, scrollState = scrollState
        )

        DragOverlay(uiState)

        // 底部过滤UI
        FilterBottomBar(
            viewModel,
            filterStr = uiState.filterStr,
            onClearFilter = { viewModel.onEvent(FileUiEvent.UpdateFilter("")) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FileScreenSideEffects(viewModel: FileViewModel, uiState: FileUiState) {
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(FileUiEvent.Refresh)
    }

    LaunchedEffect(uiState.toast) {
        if (uiState.toast.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.toast, viewModel.getString("button.confirm"))
            viewModel.onEvent(FileUiEvent.Toast(""))
        }
    }
}

@Composable
private fun rememberDragAndDropTarget(viewModel: FileViewModel): DragAndDropTarget {
    return remember(viewModel) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                viewModel.onEvent(FileUiEvent.StartDrag)
            }

            override fun onEnded(event: DragAndDropEvent) {
                viewModel.onEvent(FileUiEvent.DragEnd)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val files = extractFilesFromEvent(event)
                if (files.isNotEmpty()) {
                    viewModel.onEvent(FileUiEvent.UploadFiles(files))
                    return true
                }
                return false
            }
        }
    }
}


/**
 * 处理键盘事件的优化版本
 * @param viewModel 文件视图模型
 * @param uiState 当前UI状态
 * @param event 键盘事件
 * @return 是否消费了该事件
 */
private fun setKeyEvent(viewModel: FileViewModel, uiState: FileUiState, event: KeyEvent): Boolean {
    // 只处理按键按下事件，忽略其他类型
    if (event.type != KeyEventType.KeyDown) {
        return false
    }

    val isModifierPressed = event.isCtrlPressed || event.isMetaPressed
    val keyCode = event.key.keyCode

    // 处理带修饰键的快捷键
    if (isModifierPressed) {
        return when (keyCode) {
            Key.C.keyCode -> {
                ClipboardUtil.setSysClipboardText(uiState.parentPath)
                viewModel.onEvent(FileUiEvent.Toast(viewModel.getString("file.copyPath.success")))
                true
            }

            Key.V.keyCode -> {
                // 粘贴路径跳转功能
                viewModel.onEvent(FileUiEvent.JumpToClipboardPath)
                true
            }

            Key.R.keyCode -> {
                // Ctrl+R 刷新
                viewModel.onEvent(FileUiEvent.Refresh)
                true
            }

            else -> true // 消费其他修饰键组合，防止传递到其他组件
        }
    }

    // 处理功能键
    return when (keyCode) {
        Key.F5.keyCode -> {
            viewModel.onEvent(FileUiEvent.Refresh)
            true
        }

        Key.Escape.keyCode -> {
            handleEscapeKey(viewModel, uiState)
            true
        }

        Key.Backspace.keyCode -> {
            handleBackspaceKey(viewModel, uiState)
            true
        }

        in Key.A.keyCode..Key.Z.keyCode -> {
            // 字母键用于过滤
            val char = Char(event.key.nativeKeyCode).lowercase()
            viewModel.onEvent(FileUiEvent.UpdateFilter(uiState.filterStr + char))
            true
        }

        in Key.Zero.keyCode..Key.Nine.keyCode -> {
            // 数字键也可用于过滤
            val char = Char(event.key.nativeKeyCode)
            viewModel.onEvent(FileUiEvent.UpdateFilter(uiState.filterStr + char))
            true
        }

        else -> false
    }
}

/**
 * 处理Escape键逻辑
 */
private fun handleEscapeKey(viewModel: FileViewModel, uiState: FileUiState) {
    if (uiState.filterStr.isNotBlank()) {
        // 如果有过滤条件，先清除过滤
        viewModel.onEvent(FileUiEvent.UpdateFilter(""))
    } else {
        // 否则返回上级目录
        viewModel.onEvent(FileUiEvent.NavigateToPath(getParentPath(uiState.parentPath)))
    }
}

/**
 * 处理Backspace键逻辑
 */
private fun handleBackspaceKey(viewModel: FileViewModel, uiState: FileUiState) {
    if (uiState.filterStr.isNotBlank()) {
        // 如果有过滤条件，删除最后一个字符
        viewModel.onEvent(FileUiEvent.UpdateFilter(uiState.filterStr.dropLast(1)))
    } else {
        // 否则返回上级目录
        viewModel.onEvent(FileUiEvent.NavigateToPath(getParentPath(uiState.parentPath)))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun shouldStartDragAndDrop(event: DragAndDropEvent): Boolean {
    return try {
        val transferable = event.awtTransferable
        transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || transferable.isDataFlavorSupported(
            DataFlavor.stringFlavor
        )
    } catch (_: Exception) {
        false
    }
}


@Composable
private fun FileList(
    viewModel: FileViewModel, uiState: FileUiState, scrollState: ScrollState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(bottom = 6.dp).scrollable(scrollState, Orientation.Vertical)
    ) {
        stickyHeader {
            FileHeader(viewModel)
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (uiState.children.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center
                ) {
                    FileEmptyScreen(viewModel)
                }
            }
        } else {
            items(uiState.children) { file ->
                FileContent(file, viewModel)
            }
        }
    }

}

@Composable
private fun FilterBottomBar(
    viewModel: FileViewModel,
    filterStr: String,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = filterStr.isNotBlank(),
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = 4.dp,
            shape = MaterialTheme.shapes.medium,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = viewModel.getString("file.filter.label").format(filterStr),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 清除按钮
                IconButton(
                    onClick = onClearFilter,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Filter",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DragOverlay(uiState: FileUiState) {
    if (uiState.isDragging) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.primary.copy(alpha = 0.1f)).padding(3.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().border(
                    width = 2.dp, color = MaterialTheme.colors.primary, shape = CardShape
                ), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.uploadTipText,
                    color = MaterialTheme.colors.primary,
                    fontSize = 18.sp,
                    modifier = Modifier.clip(CardShape).background(MaterialTheme.colors.surface.copy(alpha = 0.9f))
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * 计算父级路径
 */
private fun getParentPath(currentPath: String): String {
    return if (currentPath.contains(FILE_SPLIT) && currentPath.lastIndexOf(FILE_SPLIT) > 0) {
        currentPath.substringBeforeLast(FILE_SPLIT)
    } else {
        FILE_SPLIT
    }
}