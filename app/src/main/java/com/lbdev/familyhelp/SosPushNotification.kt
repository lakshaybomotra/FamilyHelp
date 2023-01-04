package com.lbdev.familyhelp

data class SosPushNotification(
    val notification: SosNotificationData,
    val to: String,
    val data: SosPayloadData
)