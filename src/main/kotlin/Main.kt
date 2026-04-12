import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.flow.distinctUntilChanged
import me.xmbest.Config
import me.xmbest.model.Theme
import me.xmbest.module.InitModule
import me.xmbest.screen.navigation.NaviScreen

@Composable
fun App() {
    val theme = Config.theme.collectAsState().value
    MaterialTheme(
        colors =
            if (theme == Theme.System)
                if (isSystemInDarkTheme()) Theme.Night.color else Theme.Light.color
            else
                Config.theme.value.color
    ) {
        NaviScreen()
    }
}

fun main() = application {
    val windowState = Config.windowState.collectAsState()
    val viewModelStore = remember { ViewModelStore() }
    val viewModelStoreOwner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = viewModelStore
        }
    }
    Window(
        title = "EasyADB",
        onCloseRequest = ::exitApplication,
        state = windowState.value,
        icon = painterResource("icon/logo.ico")
    ) {
        LaunchedEffect(Unit) {
            snapshotFlow { windowState.value.size }
                .distinctUntilChanged()
                .collect { sizeDp ->
                    println("width: ${sizeDp.width.value},height: ${sizeDp.height.value}")
                    if (Config.getWindowSizeMode() == Config.WindowSizeMode.Remember) {
                        Config.saveRememberWindowSizeDp(sizeDp)
                    }
                }
        }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner
        ) {
            App()
            InitModule.init()
        }
    }
}