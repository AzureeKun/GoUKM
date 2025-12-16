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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScoreScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Driver Score",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFF0F4F8) // Light greyish blue bg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. High Score Main Card (Theme Blue)
            item {
                HighScoreCard()
            }

            // 2. Stats Row (Theme Yellow)
            item {
                StatsSummaryCard()
            }

            // 3. Reviews Header
            item {
                Text(
                    "Recent Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // 4. Reviews List (Clean White)
            items(reviewList) { review ->
                ReviewItemCard(review)
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun HighScoreCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        // Theme: Primary Blue
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
                text = "High",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Overall Rating:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                repeat(5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        // Theme: Yellow Stars
                        tint = Color(0xFFFFD60A), 
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bars Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Column
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    // Green for excellent feels right universally, or we can use Yellow/White
                    MetricProgress(
                        label = "Review: Excellent", 
                        progress = 1.0f, 
                        color = Color(0xFF4CAF50), // Keep Green for "Good" status
                        textColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MetricProgress(
                        label = "Reputation: Excellent", 
                        progress = 1.0f, 
                        color = Color(0xFF4CAF50),
                        textColor = Color.White
                    )
                }

                // Right Column
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    MetricProgress(
                        label = "Experience: Professional", 
                        progress = 0.8f, 
                        color = Color(0xFFFFD60A), // Theme Yellow
                        textColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MetricProgress(
                        label = "Ride Frequency: Very Low", 
                        progress = 0.2f, 
                        color = Color(0xFFEF5350), // Theme Red
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
fun StatsSummaryCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        // Theme: Secondary Yellow
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
                    text = "13",
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
            
            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color.Black.copy(alpha = 0.1f))
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "10",
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
fun ReviewItemCard(review: ReviewModel) {
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
                Text(
                    text = "1 Review",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar (Theme Blue with Icon)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE3F2FD), CircleShape), // Very light blue
                    contentAlignment = Alignment.Center
                ) {
                    Text("😊", fontSize = 24.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = review.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Text(
                            text = review.comment,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD60A),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ReviewModel(
    val name: String,
    val date: String,
    val comment: String
)

val reviewList = listOf(
    ReviewModel("RITHYA A/P ELMARAN", "18.06.2024", "Was Excellent!"),
    ReviewModel("ANGELA KELLY", "18.06.2024", "Careful!"),
    ReviewModel("FARHAN ISKANDAR", "24.04.2024", "Friendly!"),
    ReviewModel("FATTEH MUSTAFA", "08.04.2024", "Quick!")
)

@Preview
@Composable
fun DriverScoreScreenPreview() {
    MaterialTheme {
        DriverScoreScreen()
    }
}
