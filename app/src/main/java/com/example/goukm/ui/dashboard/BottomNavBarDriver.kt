package com.example.goukm.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBarDriver(selectedIndex: Int, onSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "List Request") },
            label = { Text("List Request") },
            selected = selectedIndex == 0,
            onClick = { onSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Star, contentDescription = "Score") },
            label = { Text("Score") },
            selected = selectedIndex == 1,
            onClick = { onSelected(1) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Earning") },
            label = { Text("Earning") },
            selected = selectedIndex == 2,
            onClick = { onSelected(2) }
        )
    }
}