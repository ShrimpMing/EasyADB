package me.xmbest.screen.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.xmbest.base.BaseViewModel
import me.xmbest.ddmlib.DeviceManager
import me.xmbest.ddmlib.DeviceOperate
import me.xmbest.ddmlib.Log
import me.xmbest.ddmlib.cpuCoreSize
import me.xmbest.ddmlib.ipAddress
import me.xmbest.ddmlib.memorySize
import me.xmbest.ddmlib.wmSize

class HomeViewModel() : BaseViewModel<HomeUiState>() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    override val _uiState = MutableStateFlow(HomeUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            DeviceManager.device.collectLatest { device ->
                runCatching {
                    _uiState.value = _uiState.value.copy(
                        device = device,
                        wmSize = wmSize(),
                        ipAddress = ipAddress(),
                        memory = memorySize(),
                        cpuCoreSize = cpuCoreSize(),
                    )
                }.onFailure {
                    Log.e(TAG, "onFailure!", it)
                    _uiState.value = _uiState.value.copy(device = device)
                }
            }
        }
    }


    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is HomeUiEvent.InputKey -> DeviceOperate.inputKey(event.key)
            }
        }
    }
}
