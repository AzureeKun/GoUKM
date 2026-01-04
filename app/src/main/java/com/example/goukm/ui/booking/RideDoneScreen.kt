package com.example.goukm.ui.booking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.example.goukm.ui.dashboard.BottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDoneScreen(
    navController: NavHostController,
    fareAmount: String = "RM 5",
    carBrand: String = "Perodua Myvi",
    licensePlate: String = "KFM3004",
    driverName: String = "Angie",
    onFeedbackSubmitted: (Float, String) -> Unit = { _, _ -> }
) {
    var rating by remember { mutableStateOf(0f) }
    var feedbackComment by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFE8F4FD),
        bottomBar = {
            BottomBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fixed checkmark - REMOVED invalid contentAlignment from Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ride Done",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.87f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Fixed car card Row
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically, // Added back
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Fixed profile picture icon - REMOVED invalid contentAlignment
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = fareAmount,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = carBrand,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.87f)
                        )
                        Text(
                            text = "No Plt: $licensePlate",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Rate your experience",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.87f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(5) { index ->
                        val isFilled = rating.toInt() >= (index + 1)
                        Icon(
                            imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (isFilled) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { rating = (index + 1).toFloat() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = feedbackComment,
                    onValueChange = { feedbackComment = it },
                    label = { Text("Write your feedback (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.White,
                        focusedBorderColor = Color(0xFF6B87C0),
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { onFeedbackSubmitted(rating, feedbackComment) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B87C0))
            ) {
                Text(
                    text = "Submit Feedback",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRideDone() {
   // RideDoneScreen(rememberNavController())
}
