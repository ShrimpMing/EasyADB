package me.xmbest.screen.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Toll
import androidx.lifecycle.viewModelScope
import com.android.ddmlib.IDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.xmbest.appStorageAbsolutePath
import me.xmbest.base.BaseViewModel
import me.xmbest.cmdAutoCloseEnabled
import me.xmbest.cmdAutoCloseTimeoutSeconds
import me.xmbest.ddmlib.DeviceManager
import me.xmbest.ddmlib.DeviceOperate
import me.xmbest.exec
import me.xmbest.model.Page
import me.xmbest.screen.app.AppScreen
import me.xmbest.screen.customer.CustomerScreen
import me.xmbest.screen.file.FileScreen
import me.xmbest.screen.home.HomeScreen
import me.xmbest.screen.settings.SettingsScreen
import org.jetbrains.skiko.hostOs
import java.io.File

class NaviViewModule : BaseViewModel<NaviUiState>() {
    val pageList = listOf(
        Page(
            name = getString("router.item.commonFeatures"),
            Icons.Outlined.Category
        ) {
            HomeScreen()
        },
        Page(
            getString("router.item.appManagement"),
            Icons.Outlined.GridView
        ) {
            AppScreen()
        },
        Page(
            getString("router.item.fileManagement"),
            Icons.Outlined.FolderOpen
        ) {
            FileScreen()
        },
        Page(
            getString("router.item.quickActions"),
            Icons.Outlined.Toll
        ) {
            CustomerScreen()
        },
        Page(
            getString("router.item.settings"),
            Icons.Outlined.Settings
        ) { SettingsScreen() }
    )

    override val _uiState = MutableStateFlow(NaviUiState())

    init {
        viewModelScope.launch(Dispatchers.Default) {
            DeviceManager.device.collectLatest {
                _uiState.value = _uiState.value.copy(device = it)
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            DeviceManager.devices.collectLatest {
                _uiState.value = _uiState.value.copy(devices = it)
            }
        }
    }

    fun onEvent(event: NaviUiEvent) {
        when (event) {
            is NaviUiEvent.Navigation -> handleNavigationEvent(event)
            is NaviUiEvent.DeviceManagement -> handleDeviceManagementEvent(event)
        }
    }

    private fun handleNavigationEvent(event: NaviUiEvent.Navigation) {
        when (event) {
            is NaviUiEvent.Navigation.SelectLeftItem -> selectLeftItem(event.index)
        }
    }

    private fun handleDeviceManagementEvent(event: NaviUiEvent.DeviceManagement) {
        when (event) {
            is NaviUiEvent.DeviceManagement.SelectDevice -> selectDevice(event.device)
            is NaviUiEvent.DeviceManagement.ShowDeviceList -> showDeviceList(event.show)
            is NaviUiEvent.DeviceManagement.RefreshDevice -> refreshDevice()
            is NaviUiEvent.DeviceManagement.Install -> install(event.path)
        }
    }

    private fun install(path: String) {
        DeviceOperate.install(
            remoteFilePath = path,
            isWindows = hostOs.isWindows,
            isMacOs = hostOs.isMacOS,
            autoCloseEnabled = cmdAutoCloseEnabled,
            autoCloseTimeoutSeconds = cmdAutoCloseTimeoutSeconds,
            file = File(appStorageAbsolutePath, exec.second)
        )
    }

    private fun selectLeftItem(pageIndex: Int) {
        _uiState.value = _uiState.value.copy(index = pageIndex)
    }

    private fun showDeviceList(show: Boolean) {
        _uiState.value = _uiState.value.copy(devicesListShow = show)
    }

    private fun selectDevice(iDevice: IDevice) {
        DeviceManager.changeDevice(iDevice)
        showDeviceList(false)
    }

    private fun refreshDevice() {
        DeviceManager.refreshDevices()
    }

}