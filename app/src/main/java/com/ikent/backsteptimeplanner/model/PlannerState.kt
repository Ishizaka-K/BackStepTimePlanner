package com.ikent.backsteptimeplanner.model

import java.time.*

data class PlannerState(
    val destination: String,
    val meetDate: LocalDate,
    val meetTime: LocalTime,
    val prepMin: String,
    val bufferMin: String,
    val travelMin: String
) {
    companion object {
        fun init(now: ZonedDateTime = ZonedDateTime.now()) = PlannerState(
            destination = "",
            meetDate = now.toLocalDate(),
            meetTime = LocalTime.of(19, 0),
            prepMin = "30",
            bufferMin = "10",
            travelMin = "45"
        )
    }

    private fun num(s: String) = s.toLongOrNull() ?: 0L

    fun meetAt(zone: ZoneId = ZoneId.systemDefault()): ZonedDateTime =
        ZonedDateTime.of(meetDate, meetTime, zone)

    fun departAt(): ZonedDateTime =
        meetAt().minusMinutes(num(travelMin) + num(bufferMin))

    fun wakeAt(): ZonedDateTime =
        departAt().minusMinutes(num(prepMin))

    fun isTooLate(now: ZonedDateTime = ZonedDateTime.now()): Boolean =
        now.isAfter(departAt())
}
