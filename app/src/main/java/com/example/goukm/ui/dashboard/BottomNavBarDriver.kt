package com.example.goukm.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer

// Modern color palette
private val PrimaryBlue = Color(0xFF6B87C0)
private val DarkBlue = Color(0xFF4A6199)

@Composable
fun BottomNavigationBarDriver(selectedIndex: Int, onSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = PrimaryBlue,
        tonalElevation = 0.dp,
        modifier = Modifier
            .shadow(
                elevation = 16.dp,
                spotColor = DarkBlue.copy(alpha = 0.3f)
            )
    ) {
        val items = listOf(
            Triple(Icons.Default.List, "Requests", 0),
            Triple(Icons.Default.Message, "Chat", 1),
            Triple(Icons.Default.Star, "Score", 2),
            Triple(Icons.Default.AttachMoney, "Earning", 3)
        )

        items.forEach { (icon, label, index) ->
            val isSelected = selectedIndex == index
            
            // Animation for icon scaling
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = tween(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                label = "iconScale"
            )

            // Animation for background alpha
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 0.2f else 0.0f,
                animationSpec = tween(durationMillis = 300),
                label = "bgAlpha"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelected(index) },
                icon = { 
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .background(
                                color = Color.White.copy(alpha = alpha),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = icon, 
                            contentDescription = label,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        ) 
                    }
                },
                label = { 
                    Text(
                        label,
                        color = Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}