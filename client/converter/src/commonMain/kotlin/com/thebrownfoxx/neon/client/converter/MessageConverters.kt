package com.thebrownfoxx.neon.client.converter

import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message

fun Message.toLocalMessage() = LocalMessage(
    id = id,
    groupId = groupId,
    senderId = senderId,
    content = content,
    timestamp = timestamp,
    delivery = delivery.toLocalDelivery(),
)

fun Delivery.toLocalDelivery() = when (this) {
    Delivery.Sent -> LocalDelivery.Sent
    Delivery.Delivered -> LocalDelivery.Delivered
    Delivery.Read -> LocalDelivery.Read
}