package me.xmbest

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.xmbest.locale.PropertiesLocalization
import me.xmbest.model.Environment
import me.xmbest.model.Theme
import me.xmbest.theme.blue
import me.xmbest.theme.brown
import me.xmbest.theme.cyan
import me.xmbest.theme.gray
import me.xmbest.theme.green
import me.xmbest.theme.orange
import me.xmbest.theme.pink
import me.xmbest.theme.purple
import me.xmbest.theme.red
import me.xmbest.theme.yellow
import me.xmbest.util.PreferencesUtil
import me.xmbest.util.PreferencesUtil.PREFERENCES_THEME
import java.util.*

object Config {
    const val STRINGS_NAME = "strings"

    private val _locale = MutableStateFlow(Locale.CHINA)
    val locale = _locale.asStateFlow()

    private val strings = PropertiesLocalization.create(STRINGS_NAME)

    val themeList = listOf(
        Theme.System,
        Theme.Light,
        Theme.Night,
        Theme.Other(strings.get("theme.red"), red),
        Theme.Other(strings.get("theme.orange"), orange),
        Theme.Other(strings.get("theme.yellow"), yellow),
        Theme.Other(strings.get("theme.green"), green),
        Theme.Other(strings.get("theme.cyan"), cyan),
        Theme.Other(strings.get("theme.blue"), blue),
        Theme.Other(strings.get("theme.purple"), purple),
        Theme.Other(strings.get("theme.pink"), pink),
        Theme.Other(strings.get("theme.brown"), brown),
        Theme.Other(strings.get("theme.gray"), gray)
    )

    val envList = listOf(
        Pair(strings.get("env.system"), Environment.System),
        Pair(strings.get("env.program"), Environment.Program),
        Pair(strings.get("env.custom"), Environment.Custom)
    )

    private val _windowState = MutableStateFlow(
        WindowState(
            position = WindowPosition.Aligned(Alignment.Center), size = DpSize(1280.dp, 720.dp)
        )
    )

    val windowState = _windowState.asStateFlow()

    private val currentTheme = themeList.firstOrNull {
        it.label == PreferencesUtil.get(PREFERENCES_THEME, Theme.System.label)
    } ?: Theme.System

    private val _theme = MutableStateFlow(currentTheme)

    val theme = _theme.asStateFlow()


    fun changeTheme(newTheme: Theme) {
        _theme.update { newTheme }
    }

}