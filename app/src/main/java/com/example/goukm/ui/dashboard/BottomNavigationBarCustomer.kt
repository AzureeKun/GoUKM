package com.example.goukm.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrimaryBlue = Color(0xFF6B87C0)
private val DarkBlue = Color(0xFF4A6199)

@Composable
fun BottomNavigationBarCustomer(
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
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
            Triple(Icons.Default.Home, "Home", 0),
            Triple(Icons.Default.Message, "Chat", 1),
            Triple(Icons.Default.Person, "Profile", 2)
        )
        
        items.forEach { (icon, label, index) ->
            val selected = selectedIndex == index
            
            NavigationBarItem(
                selected = selected,
                onClick = { onSelected(index) },
                icon = {
                    Box(
                        modifier = if (selected) {
                            Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        } else {
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        }
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
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
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
