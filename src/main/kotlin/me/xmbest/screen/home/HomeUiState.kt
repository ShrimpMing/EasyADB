package me.xmbest.screen.home

import com.android.ddmlib.IDevice

data class HomeUiState(
    val device: IDevice? = null,
    val wmSize: String? = null,
    val ipAddress: String? = null,
    val memory: String? = null,
    val cpuCoreSize: String? = null,
)
