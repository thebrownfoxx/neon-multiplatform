package com.thebrownfoxx.neon.ui.state.delivery

sealed interface DeliveryState {
    data object Sending : DeliveryState
    data object Sent : DeliveryState
    data object Delivered : DeliveryState
    data object Read : DeliveryState
    data class Reacted(val emoji: String) : DeliveryState
    data object Failed : DeliveryState
}