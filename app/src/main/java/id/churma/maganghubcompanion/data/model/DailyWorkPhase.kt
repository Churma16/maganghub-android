package id.churma.maganghubcompanion.data.model

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Unified representation of the intern's daily progression phases.
 * Acts as the Single Source of Truth (SSOT) across Dashboard and Widget.
 */
enum class DailyWorkPhase {
    COMPLETED,
    HOLIDAY,
    BEFORE_WORK,
    WORKING,
    REPORTING_NORMAL,
    REPORTING_URGENT;

    val isReportingPending: Boolean
        get() = this == REPORTING_NORMAL || this == REPORTING_URGENT
}

/**
 * Centralized evaluator for determining the active daily work phase.
 */
object WorkPhaseResolver {
    val ZONE_WIB: ZoneId = ZoneId.of("Asia/Jakarta")

    /**
     * Threshold under which reporting is considered urgent (in hours).
     * Set to 4 hours per product specification.
     */
    const val URGENT_THRESHOLD_HOURS = 4L
    const val URGENT_THRESHOLD_MILLIS = URGENT_THRESHOLD_HOURS * 60 * 60 * 1000L // 14,400,000 ms

    /**
     * Resolves the current daily work phase based on attendance status, schedule,
     * reminder settings, and current time in WIB.
     */
    fun resolve(
        hasAttendance: Boolean,
        isHoliday: Boolean,
        workStartHour: Int,
        workStartMinute: Int,
        reportStartHour: Int,
        nowWib: ZonedDateTime = ZonedDateTime.now(ZONE_WIB),
        remainingMillis: Long = calculateRemainingMillis(nowWib)
    ): DailyWorkPhase {
        if (hasAttendance) return DailyWorkPhase.COMPLETED
        if (isHoliday) return DailyWorkPhase.HOLIDAY

        val currentMinutes = nowWib.hour * 60 + nowWib.minute
        val workStartMinutes = workStartHour * 60 + workStartMinute
        val reportStartMinutes = reportStartHour * 60

        return when {
            currentMinutes < workStartMinutes -> DailyWorkPhase.BEFORE_WORK
            currentMinutes < reportStartMinutes -> DailyWorkPhase.WORKING
            remainingMillis < URGENT_THRESHOLD_MILLIS -> DailyWorkPhase.REPORTING_URGENT
            else -> DailyWorkPhase.REPORTING_NORMAL
        }
    }

    /**
     * Calculates milliseconds remaining until 23:59:59.999 WIB of the current date.
     */
    fun calculateRemainingMillis(nowWib: ZonedDateTime = ZonedDateTime.now(ZONE_WIB)): Long {
        val deadline = nowWib.toLocalDate().atTime(23, 59, 59, 999_000_000).atZone(ZONE_WIB)
        val duration = Duration.between(nowWib, deadline)
        return duration.toMillis().coerceAtLeast(0L)
    }
}
