package me.xmbest.screen.home

sealed class HomeUiEvent {
    class InputKey(val key: Int) : HomeUiEvent()
}