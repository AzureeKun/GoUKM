package com.example.goukm.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.ui.booking.Booking
import com.example.goukm.ui.booking.BookingRepository
import com.example.goukm.ui.booking.BookingStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class DriverEarningViewModel(
    private val repository: BookingRepository = BookingRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow("Day")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    private val _graphGranularity = MutableStateFlow("Hour")
    val graphGranularity: StateFlow<String> = _graphGranularity.asStateFlow()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    
    val uiState = combine(_bookings, _selectedPeriod, _currentDate, _graphGranularity) { bookings, period, date, granularity ->
        val filtered = filterBookings(bookings, period, date)
        val aggregatedData = aggregateData(filtered, period, date, granularity)
        val totalEarnings = filtered.sumOf { it.offeredFare.toDoubleOrNull() ?: 0.0 }
        val rideCount = filtered.size
        
        EarningUiState(
            totalEarnings = totalEarnings,
            rideCount = rideCount,
            graphData = aggregatedData,
            recentRide = filtered.maxByOrNull { it.timestamp }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EarningUiState())

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                repository.getBookingsByDriverAndStatus(currentUser.uid, BookingStatus.COMPLETED)
                    .collect { _bookings.value = it }
            }
        }
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
        // Update granularity default
        _graphGranularity.value = when (period) {
            "Day" -> "Hour"
            "Week" -> "Day"
            "Month" -> "Day"
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

    private fun filterBookings(bookings: List<Booking>, period: String, date: LocalDate): List<Booking> {
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

        return bookings.filter { booking ->
            val bDate = booking.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            !bDate.isBefore(start) && !bDate.isAfter(end)
        }
    }

    private fun aggregateData(bookings: List<Booking>, period: String, date: LocalDate, granularity: String): List<BarData> {
        return when (granularity) {
            "Hour" -> {
                val hours = (6..23).map { if (it < 12) "${it}AM" else if (it == 12) "12PM" else "${it-12}PM" }
                hours.map { label ->
                    val count = bookings.count { b ->
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
                        val count = bookings.count { b ->
                            val bDate = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            bDate == targetDate
                        }
                        BarData(label, count.toFloat())
                    }
                } else if (period == "Month") {
                    val daysInMonth = date.lengthOfMonth()
                    (1..daysInMonth).map { day ->
                        val count = bookings.count { b ->
                            val bDate = b.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            bDate.dayOfMonth == day
                        }
                        BarData(if (day % 5 == 0 || day == 1) "$day" else "", count.toFloat())
                    }
                } else emptyList()
            }
            "Month" -> {
                val months = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
                months.mapIndexed { index, label ->
                    val count = bookings.count { b ->
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
}

data class EarningUiState(
    val totalEarnings: Double = 0.0,
    val rideCount: Int = 0,
    val graphData: List<BarData> = emptyList(),
    val recentRide: Booking? = null
)
