package me.newbieeming.screen.customer

import me.newbieeming.screen.customer.entity.BaseFastBroadData

data class CustomerUiState(
    val configList: List<BaseFastBroadData> = emptyList(),
    val toast: String = "",
    val inputValues: Map<String, String> = emptyMap() // uuid -> value mapping for persistent input
)
