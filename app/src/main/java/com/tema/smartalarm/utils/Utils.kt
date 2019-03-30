package com.tema.smartalarm.utils

import java.util.*

fun getTimeFromText(text: String): Calendar {
    val timeList = text.split(':')
    val calendar = Calendar.getInstance()
    with(calendar) {
        val time = (timeList[0] + timeList[1]).toInt()
        add(Calendar.HOUR_OF_DAY, timeZone.rawOffset / 3600000)
        val currentTime = (get(Calendar.HOUR_OF_DAY).toString() + get(Calendar.MINUTE).toString()).toInt()
        if (time < currentTime)
            add(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, timeList[0].toInt())
        set(Calendar.MINUTE, timeList[1].toInt())
    }
    return calendar
}