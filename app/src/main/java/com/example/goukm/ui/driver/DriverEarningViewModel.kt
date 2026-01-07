package com.example.goukm.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.ui.booking.Booking
import com.example.goukm.ui.booking.BookingRepository
import com.example.goukm.ui.booking.BookingStatus
import com.example.goukm.ui.booking.Journey
import com.example.goukm.ui.booking.JourneyRepository
import com.example.goukm.ui.userprofile.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.goukm.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class DriverEarningViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val bookingRepository: BookingRepository = BookingRepository()
    private val journeyRepository: JourneyRepository = JourneyRepository
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val sessionManager = SessionManager(application)

    private val _selectedPeriod = MutableStateFlow("Day")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    private val _graphGranularity = MutableStateFlow("Hour")
    val graphGranularity: StateFlow<String> = _graphGranularity.asStateFlow()

    private val _journeys = MutableStateFlow<List<Journey>>(emptyList())
    private val _onlineDurations = MutableStateFlow<Map<String, Long>>(emptyMap())

    // Tracks duration of the current active session in minutes
    private val _currentSessionMinutes = MutableStateFlow(0L)
    
    // Combine filter-related states first to reduce arity for the main combine
    private val _filterState = combine(_selectedPeriod, _currentDate, _graphGranularity) { period, date, granularity ->
        Triple(period, date, granularity)
    }

    val uiState = combine(_journeys, _onlineDurations, _currentSessionMinutes, _filterState) { journeys, onlineDurations, sessionMinutes, filterState ->
        val (period, date, granularity) = filterState
        
        val filtered = filterJourneys(journeys, period, date)
        val aggregatedData = aggregateData(filtered, period, date, granularity)
        val totalEarnings = filtered.sumOf { it.offeredFare.toDoubleOrNull() ?: 0.0 }
        val rideCount = filtered.size
        
        var totalOnlineMinutes = aggregateOnlineMinutes(onlineDurations, period, date)
        
        // Add current session minutes if the selected date includes "Today"
        val today = LocalDate.now()
        val isTodayIncluded = when(period) {
            "Day" -> date == today
            "Week" -> !today.isBefore(date.with(java.time.DayOfWeek.MONDAY)) && !today.isAfter(date.with(java.time.DayOfWeek.SUNDAY))
            "Month" -> date.month == today.month && date.year == today.year
            "Year" -> date.year == today.year
            else -> false
        }
        
        if (isTodayIncluded) {
            totalOnlineMinutes += sessionMinutes
        }
        
        EarningUiState(
            totalEarnings = totalEarnings,
            rideCount = rideCount,
            graphData = aggregatedData,
            totalOnlineMinutes = totalOnlineMinutes,
            recentRide = filtered.maxByOrNull { it.timestamp }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EarningUiState())

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                journeyRepository.getJourneysByDriver(currentUser.uid)
                    .collect { _journeys.value = it }
            }
            viewModelScope.launch {
                val profile = UserProfileRepository.getUserProfile(currentUser.uid)
                _onlineDurations.value = profile?.onlineWorkDurations ?: emptyMap()
            }
        }
        
        // Start Real-time Session Timer
        viewModelScope.launch {
            while (isActive) {
                val startTime = sessionManager.fetchOnlineStartTime()
                if (startTime > 0) {
                    val now = System.currentTimeMillis()
                    val diff = now - startTime
                    val minutes = diff / (1000 * 60)
                    _currentSessionMinutes.value = minutes
                } else {
                    _currentSessionMinutes.value = 0L
                }
                delay(60000) // Update every minute
            }
        }
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
        // Update granularity default
        _graphGranularity.value = when (period) {
            "Day" -> "Hour"
            "Week" -> "Day"
            "Month" -> "Week"
            "Year" -> "Month"
            else -> "Day"
        }
    }

    fun setGranularity(granularity: String) {
        _graphGranularity.value = granularity
    }

    fun moveDateByEpoch(millis: Long) {
        _currentDate.value = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun moveDate(direction: Int) {
        val current = _currentDate.value
        _currentDate.value = when (_selectedPeriod.value) {
            "Day" -> current.plusDays(direction.toLong())
            "Week" -> current.plusWeeks(direction.toLong())
            "Month" -> current.plusMonths(direction.toLong())
            "Year" -> current.plusYears(direction.toLong())
            else -> current
        }
    }

    private fun filterJourneys(journeys: List<Journey>, period: String, date: LocalDate): List<Journey> {
        val start: LocalDate
        val end: LocalDate

        when (period) {
            "Day" -> {
                start = date
                end = date
            }
            "Week" -> {
                start = date.with(java.time.DayOfWeek.MONDAY)
                end = date.with(java.time.DayOfWeek.SUNDAY)
            }
            "Month" -> {
                start = date.withDayOfMonth(1)
                end = date.withDayOfMonth(date.lengthOfMonth())
            }
            "Year" -> {
                start = date.withDayOfYear(1)
                end = date.withDayOfYear(date.lengthOfYear())
            }
            else -> return emptyList()
        }

        return journeys.filter { journey ->
            val bDate = journey.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            !bDate.isBefore(start) && !bDate.isAfter(end)
        }
    }

    private fun aggregateData(journeys: List<Journey>, period: String, date: LocalDate, granularity: String): List<BarData> {
        return when (granularity) {
            "Hour" -> {
                val hours = (6..23).map { if (it < 12) "${it}AM" else if (it == 12) "12PM" else "${it-12}PM" }
                hours.map { label ->
                    val count = journeys.count { b ->
                        val bHour = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).hour
                        val labelHour = parseHourLabel(label)
                        bHour == labelHour
                    }
                    BarData(label, count.toFloat())
                }
            }
            "Day" -> {
                if (period == "Week") {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.mapIndexed { index, label ->
                        val targetDate = date.with(java.time.DayOfWeek.MONDAY).plusDays(index.toLong())
                        val count = journeys.count { b ->
                            val bDate = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            bDate == targetDate
                        }
                        BarData(label, count.toFloat())
                    }
                } else if (period == "Month") {
                    val daysInMonth = date.lengthOfMonth()
                    (1..daysInMonth).map { day ->
                        val count = journeys.count { b ->
                            val bDate = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            bDate.dayOfMonth == day
                        }
                        BarData(if (day % 5 == 0 || day == 1) "$day" else "", count.toFloat())
                    }
                } else if (period == "Year") {
                    // Show days of the year (simplified to 1-365/366 or grouped?)
                    // The request says "display days for x-axis" for Year. 
                    // Showing 365 bars is too many for a mobile chart unless it's scrollable.
                    // Let's group by month and show ticks, or just show month starts.
                    // For now, let's group by day and show sparse labels.
                    val daysInYear = if (date.isLeapYear) 366 else 365
                    (1..daysInYear).map { dayOfYear ->
                        val count = journeys.count { b ->
                           val bDate = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                           bDate.year == date.year && bDate.dayOfYear == dayOfYear
                        }
                        BarData(if (dayOfYear % 30 == 1) "Day $dayOfYear" else "", count.toFloat())
                    }
                } else emptyList()
            }
            "Week" -> {
                if (period == "Month") {
                    // Weeks in month
                    val firstDay = date.withDayOfMonth(1)
                    val lastDay = date.withDayOfMonth(date.lengthOfMonth())
                    
                    // Simple approach: Divide by 7
                    val weeks = mutableListOf<BarData>()
                    var currentStart = firstDay
                    var weekNum = 1
                    while (!currentStart.isAfter(lastDay)) {
                        val currentEnd = currentStart.plusDays(6).let { if (it.isAfter(lastDay)) lastDay else it }
                        val count = journeys.count { b ->
                            val bDate = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            !bDate.isBefore(currentStart) && !bDate.isAfter(currentEnd)
                        }
                        weeks.add(BarData("W$weekNum", count.toFloat()))
                        currentStart = currentEnd.plusDays(1)
                        weekNum++
                    }
                    weeks
                } else if (period == "Year") {
                    // Weeks in year
                    (1..52).map { weekIndex ->
                        val count = journeys.count { b ->
                            val bDate = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            val weekFields = java.time.temporal.WeekFields.of(Locale.getDefault())
                            bDate.year == date.year && bDate.get(weekFields.weekOfYear()) == weekIndex
                        }
                        BarData(if (weekIndex % 4 == 1) "W$weekIndex" else "", count.toFloat())
                    }
                } else emptyList()
            }
            "Month" -> {
                val months = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
                months.mapIndexed { index, label ->
                    val count = journeys.count { b ->
                        val bMonth = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).monthValue
                        bMonth == (index + 1)
                    }
                    BarData(label, count.toFloat())
                }
            }
            else -> emptyList()
        }
    }

    private fun parseHourLabel(label: String): Int {
        val isPm = label.endsWith("PM")
        val num = label.substring(0, label.length - 2).toInt()
        return if (isPm && num != 12) num + 12 else if (!isPm && num == 12) 0 else num
    }

    private fun aggregateOnlineMinutes(durations: Map<String, Long>, period: String, date: LocalDate): Long {
        val start: LocalDate
        val end: LocalDate

        when (period) {
            "Day" -> {
                start = date
                end = date
            }
            "Week" -> {
                start = date.with(java.time.DayOfWeek.MONDAY)
                end = date.with(java.time.DayOfWeek.SUNDAY)
            }
            "Month" -> {
                start = date.withDayOfMonth(1)
                end = date.withDayOfMonth(date.lengthOfMonth())
            }
            "Year" -> {
                start = date.withDayOfYear(1)
                end = date.withDayOfYear(date.lengthOfYear())
            }
            else -> return 0L
        }

        var total = 0L
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        var current = start
        while (!current.isAfter(end)) {
            val key = current.format(formatter)
            total += durations[key] ?: 0L
            current = current.plusDays(1)
        }
        
        return total
    }
}

data class EarningUiState(
    val totalEarnings: Double = 0.0,
    val rideCount: Int = 0,
    val graphData: List<BarData> = emptyList(),
    val totalOnlineMinutes: Long = 0,
    val recentRide: Journey? = null
)
