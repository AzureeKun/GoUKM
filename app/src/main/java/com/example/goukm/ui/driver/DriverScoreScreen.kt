package com.example.goukm.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.dashboard.BottomNavigationBarDriver

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.example.goukm.ui.booking.RatingRepository
import com.example.goukm.ui.booking.Rating
import com.example.goukm.ui.booking.DriverStats
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScoreScreen(navController: NavHostController) {
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    var driverStats by remember { mutableStateOf(DriverStats()) }
    var reviews by remember { mutableStateOf<List<com.example.goukm.ui.booking.Rating>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            isLoading = true
            // Fetch Stats
            driverStats = RatingRepository.getDriverStats(currentUserId)
            
            // Fetch Reviews
            val result = RatingRepository.getRatingsForDriver(currentUserId)
            result.onSuccess { 
                reviews = it 
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Driver Score",
                        color = Color(0xFF1E293B), // Dark Navy
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFF8FAFC) // Unified Modern Gray-White
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6B87C0))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. High Score Main Card (Theme Blue)
                item {
                    HighScoreCard(driverStats)
                }

                // 2. Stats Row (Theme Yellow)
                item {
                    StatsSummaryCard(driverStats)
                }

                // 3. Reviews Header
                item {
                    Text(
                        "Recent Ride Reviews",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // 4. Reviews List (Clean White)
                if (reviews.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                             Text(
                                "No reviews yet.",
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.Gray
                             )
                        }
                    }
                } else {
                    items(reviews) { review ->
                        ReviewItemCard(review)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun HighScoreCard(stats: DriverStats) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6B87C0)), 
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (stats.isNewDriver) "New Driver" else String.format("%.1f", stats.averageRating),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!stats.isNewDriver) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Overall Rating:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    repeat(5) { index ->
                        val isFilled = stats.averageRating >= (index + 0.5f)
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isFilled) Color(0xFFFFD60A) else Color.White.copy(alpha = 0.3f), 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                 Text(
                    text = "Welcome to GoUKM!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bars Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    val reviewLabel = when {
                        stats.isNewDriver -> "Review: N/A"
                        stats.averageRating >= 4.5 -> "Review: Excellent"
                        stats.averageRating >= 3.5 -> "Review: Good"
                        else -> "Review: Average"
                    }
                    val reviewProgress = if (stats.isNewDriver) 0f else stats.averageRating / 5f
                    
                    MetricProgress(
                        label = reviewLabel, 
                        progress = reviewProgress, 
                        color = if (reviewProgress >= 0.8f) Color(0xFF4CAF50) else Color(0xFFFFD60A),
                        textColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val reputationProgress = (stats.totalWorkComplete / 20f).coerceIn(0f, 1f)
                    val reputationLabel = when {
                        reputationProgress >= 0.9f -> "Reputation: Excellent"
                        reputationProgress >= 0.5f -> "Reputation: Good"
                        else -> "Reputation: New"
                    }
                    
                    MetricProgress(
                        label = reputationLabel, 
                        progress = reputationProgress, 
                        color = if (reputationProgress >= 0.5f) Color(0xFF4CAF50) else Color(0xFFFFD60A),
                        textColor = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    val expProgress = (stats.totalWorkComplete / 50f).coerceIn(0f, 1f)
                    val expLabel = when {
                        expProgress >= 0.8f -> "Experience: Professional"
                        expProgress >= 0.4f -> "Experience: Regular"
                        else -> "Experience: Novice"
                    }
                    
                    MetricProgress(
                        label = expLabel, 
                        progress = expProgress, 
                        color = if (expProgress >= 0.8f) Color(0xFFFFD60A) else Color(0xFF4CAF50),
                        textColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val jobsPerDay = stats.totalWorkComplete.toFloat() / stats.daysWorked.coerceAtLeast(1).toFloat()
                    val frequencyProgress = (jobsPerDay / 5f).coerceIn(0f, 1f)
                    val frequencyLabel = "Ride Frequency: ${stats.totalWorkComplete} rides / ${stats.daysWorked} days"
                    
                    MetricProgress(
                        label = frequencyLabel, 
                        progress = frequencyProgress, 
                        color = if (frequencyProgress >= 0.5f) Color(0xFF4CAF50) else Color(0xFFFFD60A),
                        textColor = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MetricProgress(label: String, progress: Float, color: Color, textColor: Color) {
    Column {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = Color.Black.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = textColor.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun StatsSummaryCard(stats: DriverStats) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD60A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stats.totalWorkComplete.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    text = "Total Work\nComplete",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color.Black.copy(alpha = 0.1f))
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stats.totalReviews.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ReviewItemCard(review: com.example.goukm.ui.booking.Rating) {
    val dateStr = remember(review.timestamp) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        sdf.format(Date(review.timestamp))
    }
    
    val ratingEmoji = when(review.rating.toInt()) {
        5 -> "🤩"
        4 -> "😊"
        3 -> "😐"
        2 -> "🙁"
        1 -> "😠"
        else -> "🤔"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        val isFilled = review.rating >= (index + 1)
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isFilled) Color(0xFFFFD60A) else Color.LightGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${review.rating.toInt()} Stars $ratingEmoji",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = when(review.rating.toInt()) {
                                5 -> Color(0xFFE8F5E9) // Light Green
                                4 -> Color(0xFFF1F8E9) // Light Lime
                                3 -> Color(0xFFFFF3E0) // Light Orange
                                2 -> Color(0xFFFFFDE7) // Light Yellow
                                1 -> Color(0xFFFFEBEE) // Light Red
                                else -> Color(0xFFF5F5F5)
                            }, 
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ratingEmoji, fontSize = 24.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    if (review.pickup.isNotEmpty() && review.dropOff.isNotEmpty()) {
                        Text(
                            text = "To ${review.dropOff}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    if (review.comment.isNotEmpty()) {
                        Text(
                            text = review.comment,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

