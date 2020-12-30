package org.gundartsev.edu.sensors

import java.time.OffsetDateTime
import java.time.ZoneOffset

fun dateTime(offsetDataTime: String) = OffsetDateTime.parse("$offsetDataTime+00:00")
fun minuteId(offsetDataTime: String) = (dateTime(offsetDataTime).withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond() / 60).toInt()
fun hourId(offsetDataTime: String) = minuteId(offsetDataTime) / 60
