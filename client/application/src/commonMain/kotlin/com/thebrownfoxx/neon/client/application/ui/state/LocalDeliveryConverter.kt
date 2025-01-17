package com.thebrownfoxx.neon.client.application.ui.state

import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.model.LocalDelivery

fun LocalDelivery.toDeliveryState() = when (this) {
    LocalDelivery.Sending -> DeliveryState.Sending
    LocalDelivery.Sent -> DeliveryState.Sent
    LocalDelivery.Delivered -> DeliveryState.Delivered
    LocalDelivery.Read -> DeliveryState.Read
    LocalDelivery.Failed -> DeliveryState.Failed
}