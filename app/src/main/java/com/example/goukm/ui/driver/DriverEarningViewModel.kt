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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private val _journeysByDate = MutableStateFlow<Map<LocalDate, List<Journey>>>(emptyMap())

    // Pre-calculate historic state to avoid expensive aggregation on every timer tick
    private val _historicState = combine(_journeysByDate, _onlineDurations, _filterState) { journeysByDate, onlineDurations, filterState ->
        if (journeysByDate.isEmpty() && onlineDurations.isEmpty()) {
            return@combine HistoricData(0.0, 0, emptyList(), 0L, null, false)
        }
        
        val (period, date, granularity) = filterState
        
        val filtered = filterJourneysMap(journeysByDate, period, date)
        val aggregatedData = aggregateDataMap(journeysByDate, period, date, granularity)
        val totalEarnings = filtered.sumOf { it.offeredFare.toDoubleOrNull() ?: 0.0 }
        val rideCount = filtered.size
        
        val historicOnlineMinutes = aggregateOnlineMinutes(onlineDurations, period, date)
        
        // Check if today is included in the current selection
        val today = LocalDate.now()
        val isTodayIncluded = when(period) {
            "Day" -> date == today
            "Week" -> !today.isBefore(date.with(java.time.DayOfWeek.MONDAY)) && !today.isAfter(date.with(java.time.DayOfWeek.SUNDAY))
            "Month" -> date.month == today.month && date.year == today.year
            "Year" -> date.year == today.year
            else -> false
        }

        HistoricData(
            totalEarnings = totalEarnings,
            rideCount = rideCount,
            graphData = aggregatedData,
            historicOnlineMinutes = historicOnlineMinutes,
            recentRide = filtered.maxByOrNull { it.timestamp },
            isTodayIncluded = isTodayIncluded
        )
    }.distinctUntilChanged().flowOn(Dispatchers.Default)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val uiState = combine(_historicState, _currentSessionMinutes) { historic, sessionMinutes ->
        EarningUiState(
            totalEarnings = historic.totalEarnings,
            rideCount = historic.rideCount,
            graphData = historic.graphData,
            totalOnlineMinutes = historic.historicOnlineMinutes + (if (historic.isTodayIncluded) sessionMinutes else 0L),
            recentRide = historic.recentRide,
            isLoading = _isLoading.value
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EarningUiState(isLoading = true))

    private data class HistoricData(
        val totalEarnings: Double = 0.0,
        val rideCount: Int = 0,
        val graphData: List<BarData> = emptyList(),
        val historicOnlineMinutes: Long = 0L,
        val recentRide: Journey? = null,
        val isTodayIncluded: Boolean = false
    )


    init {
        val currentUser = auth.currentUser
        _isLoading.value = false // Show UI instantly
        
        if (currentUser != null) {
            viewModelScope.launch {
                journeyRepository.getJourneysByDriver(currentUser.uid)
                    .collect { list ->
                        _journeys.value = list
                        
                        // Pre-aggregate by date for faster filtering
                        // Optimization: Use a local variable to zip date and avoid repeated expensive conversions
                        val grouped = withContext(Dispatchers.Default) {
                            val zoneId = ZoneId.systemDefault()
                            list.groupBy { 
                                it.timestamp.toInstant().atZone(zoneId).toLocalDate()
                            }
                        }
                        _journeysByDate.value = grouped
                    }
            }
            viewModelScope.launch {
                val profile = com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(currentUser.uid)
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

    private fun filterJourneysMap(journeysByDate: Map<LocalDate, List<Journey>>, period: String, date: LocalDate): List<Journey> {
        val start: LocalDate
        val end: LocalDate

        when (period) {
            "Day" -> return journeysByDate[date] ?: emptyList()
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

        val result = mutableListOf<Journey>()
        var current = start
        while (!current.isAfter(end)) {
            journeysByDate[current]?.let { result.addAll(it) }
            current = current.plusDays(1)
        }
        return result
    }

    private fun aggregateDataMap(journeysByDate: Map<LocalDate, List<Journey>>, period: String, date: LocalDate, granularity: String): List<BarData> {
        return when (granularity) {
            "Hour" -> {
                val journeys = journeysByDate[date] ?: emptyList()
                val hours = (6..23).map { if (it < 12) "${it}AM" else if (it == 12) "12PM" else "${it-12}PM" }
                hours.map { label ->
                    val labelHour = parseHourLabel(label)
                    val count = journeys.count { b ->
                        val bHour = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).hour
                        bHour == labelHour
                    }
                    BarData(label, count.toFloat())
                }
            }
            "Day" -> {
                val (start, count, labels) = when(period) {
                    "Week" -> Triple(date.with(java.time.DayOfWeek.MONDAY), 7, listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
                    "Month" -> Triple(date.withDayOfMonth(1), date.lengthOfMonth(), null)
                    "Year" -> Triple(date.withDayOfYear(1), if (date.isLeapYear) 366 else 365, null)
                    else -> Triple(date, 1, null)
                }

                (0 until count).map { i ->
                    val currentDate = start.plusDays(i.toLong())
                    val journeys = journeysByDate[currentDate] ?: emptyList()
                    val label = when(period) {
                        "Week" -> labels?.get(i) ?: ""
                        "Month" -> {
                            val day = i + 1
                            if (day % 5 == 0 || day == 1) "$day" else ""
                        }
                        "Year" -> {
                            val dayOfYear = i + 1
                            if (dayOfYear % 30 == 1) "Day $dayOfYear" else ""
                        }
                        else -> ""
                    }
                    BarData(label, journeys.size.toFloat())
                }
            }
            "Week" -> {
                val start = if (period == "Month") date.withDayOfMonth(1) else date.withDayOfYear(1)
                val end = if (period == "Month") date.withDayOfMonth(date.lengthOfMonth()) else date.withDayOfYear(date.lengthOfYear())

                if (period == "Month") {
                    val weeks = mutableListOf<BarData>()
                    var currentStart = start
                    var weekNum = 1
                    while (!currentStart.isAfter(end)) {
                        val currentEnd = currentStart.plusDays(6).let { if (it.isAfter(end)) end else it }
                        var count = 0
                        var temp = currentStart
                        while(!temp.isAfter(currentEnd)) {
                            count += journeysByDate[temp]?.size ?: 0
                            temp = temp.plusDays(1)
                        }
                        weeks.add(BarData("W$weekNum", count.toFloat()))
                        currentStart = currentEnd.plusDays(1)
                        weekNum++
                    }
                    weeks
                } else if (period == "Year") {
                    // Pre-aggregate year journeys by week to avoid nested loops
                    val yearJourneys = journeysByDate.filter { it.key.year == date.year }
                    val weekFields = java.time.temporal.WeekFields.of(Locale.getDefault())
                    val weekMap = mutableMapOf<Int, Int>()
                    yearJourneys.forEach { (d, list) ->
                        val w = d.get(weekFields.weekOfYear())
                        weekMap[w] = (weekMap[w] ?: 0) + list.size
                    }
                    (1..52).map { w ->
                        BarData(if (w % 4 == 1) "W$w" else "", (weekMap[w] ?: 0).toFloat())
                    }
                } else emptyList()
            }
            "Month" -> {
                val months = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
                // Pre-aggregate by month
                val yearJourneys = journeysByDate.filter { it.key.year == date.year }
                val monthMap = IntArray(12)
                yearJourneys.forEach { (d, list) ->
                    monthMap[d.monthValue - 1] += list.size
                }
                months.mapIndexed { index, label ->
                    BarData(label, monthMap[index].toFloat())
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
    val recentRide: Journey? = null,
    val isLoading: Boolean = false
)
