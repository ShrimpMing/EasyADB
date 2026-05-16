package me.newbieeming.screen.customer.entity

data class InputSendData(
    val title: String,
    val cmd: String,
    val template: String,
    val hint: String = "",
    val btnText: String = "Send"
) : BaseFastBroadData(FastBroadType.INPUT_SEND)
