package me.xmbest.ddmlib

import com.android.ddmlib.IDevice

val IDevice.androidVersion: String?
    get() = getProperty("ro.build.version.release")

val IDevice.buildDate: String?
    get() = getProperty("ro.build.date")

val IDevice.buildType: String?
    get() = getProperty("ro.build.type")

val IDevice.productBrand: String?
    get() = getProperty("ro.product.brand")

val IDevice.productName: String?
    get() = getProperty("ro.product.name")

val IDevice.socModel: String?
    get() = getProperty("ro.soc.model")

suspend fun cpuCoreSize() = DeviceOperate.shell("cat /proc/cpuinfo | grep processor | wc -l", 200)

suspend fun wmSize() = DeviceOperate.shell("wm size | awk '{print \$NF}'", 200)

suspend fun ipAddress() = DeviceOperate.shell("ifconfig wlan0 |  grep addr:1 |  awk  '{print $2}'", 200)

suspend fun memorySize() = DeviceOperate.shell("cat /proc/meminfo | grep MemTotal | awk '{print \$2/1024}'", 200)

suspend fun batteryLevel() = DeviceOperate.shell("dumpsys battery | grep level| awk '{print \$2}'", 200)
