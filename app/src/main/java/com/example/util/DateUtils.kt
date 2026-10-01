package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val displayMonthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun todayIso(): String {
        return isoFormat.format(Date())
    }

    fun currentYearMonth(): String {
        return monthYearFormat.format(Date())
    }

    fun startOfWeekIso(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return isoFormat.format(cal.time)
    }

    fun formatDisplayDate(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate)
            if (date != null) displayDateFormat.format(date) else isoDate
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatDisplayMonthYear(yearMonth: String): String {
        return try {
            val date = monthYearFormat.parse(yearMonth)
            if (date != null) displayMonthYearFormat.format(date) else yearMonth
        } catch (_: Exception) {
            yearMonth
        }
    }

    fun calculateWorkingHours(startTime: String, endTime: String): Double {
        if (startTime.isBlank() || endTime.isBlank()) return 0.0
        return try {
            val startMinutes = parseTimeToMinutes(startTime)
            val endMinutes = parseTimeToMinutes(endTime)
            if (startMinutes == null || endMinutes == null) return 0.0

            val diff = if (endMinutes >= startMinutes) {
                endMinutes - startMinutes
            } else {
                // overnight wrap-around
                (endMinutes + 1440) - startMinutes
            }
            Math.round((diff / 60.0) * 100.0) / 100.0
        } catch (_: Exception) {
            0.0
        }
    }

    private fun parseTimeToMinutes(timeStr: String): Int? {
        val trimmed = timeStr.trim().lowercase(Locale.US)
        val isPm = trimmed.contains("pm")
        val isAm = trimmed.contains("am")
        val clean = trimmed.replace("am", "").replace("pm", "").trim()
        val parts = clean.split(":")
        if (parts.isEmpty()) return null

        var hour = parts[0].toIntOrNull() ?: return null
        val minute = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0

        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        return hour * 60 + minute
    }

    fun formatHours(hours: Double): String {
        return if (hours <= 0.0) "0h" else {
            val h = hours.toInt()
            val m = Math.round((hours - h) * 60).toInt()
            if (m == 0) "${h}h" else "${h}h ${m}m"
        }
    }

    fun formatMinutesDuration(minutes: Double): String {
        if (minutes <= 0.0) return "0 min"
        val totalMins = minutes.toInt()
        val hours = totalMins / 60
        val remainingMins = (minutes - (hours * 60))
        return if (hours > 0) {
            if (remainingMins % 1.0 == 0.0) {
                String.format(Locale.US, "%dh %02dm", hours, remainingMins.toInt())
            } else {
                String.format(Locale.US, "%dh %.1fm", hours, remainingMins)
            }
        } else {
            if (minutes % 1.0 == 0.0) "${minutes.toInt()} min" else String.format(Locale.US, "%.1f min", minutes)
        }
    }

    fun generateYearMonthsList(): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 3)
        for (i in 0 until 36) {
            list.add(monthYearFormat.format(cal.time))
            cal.add(Calendar.MONTH, -1)
        }
        return list
    }
}
