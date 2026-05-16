package me.newbieeming.screen.customer.entity

import java.util.*

open class BaseFastBroadData(
    val type: String,
    val uuid: String = "uuid-${UUID.randomUUID()}"
)
