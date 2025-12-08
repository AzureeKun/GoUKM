package com.example.goukm.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.userprofile.CBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestScreen(navController: NavHostController) {
    var selectedSeat by remember { mutableStateOf("4-Seat") }
    var pickup by remember { mutableStateOf("Kolej Aminuddin Baki") }
    var dropOff by remember { mutableStateOf("Kolej Pendeta Za'ba") }

    val mapPlaceholder = Color(0xFFE6ECF4)
    val textFieldBg = Color(0xFFF2F3F5)
    val accentYellow = Color(0xFFFFD60A)

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true // avoid fully hidden; keeps sheet draggable back up
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp,
        sheetContainerColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetDragHandle = {}, // disable default handle; we draw our own
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .background(Color.LightGray, RoundedCornerShape(50))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SeatChip(
                        label = "4-Seat",
                        isSelected = selectedSeat == "4-Seat",
                        onClick = { selectedSeat = "4-Seat" }
                    )
                    Spacer(Modifier.width(12.dp))
                    SeatChip(
                        label = "6-Seat",
                        isSelected = selectedSeat == "6-Seat",
                        onClick = { selectedSeat = "6-Seat" }
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Pickup Point", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = pickup,
                            onValueChange = { pickup = it },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Pickup",
                                    tint = Color.Black
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = textFieldBg,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        Text("Drop-Off Point", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = dropOff,
                            onValueChange = { dropOff = it },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Drop-off",
                                    tint = Color.Black
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = textFieldBg,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Button(
                    onClick = {
                        // TODO: hook to booking flow
                        navController.navigate(NavRoutes.CustomerDashboard.route) {
                            popUpTo(NavRoutes.BookingRequest.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentYellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Booking Ride", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        // Map area stays full screen; sheet slides over it
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(mapPlaceholder),
            contentAlignment = Alignment.Center
        ) {
            Text("Map preview", color = Color.Gray)
        }
    }
}

@Composable
private fun RowScope.SeatChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) CBlue else Color(0xFFE0E6F3)
    val contentColor = if (isSelected) Color.White else Color.Black

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .weight(1f)
            .height(44.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        Text(label, color = contentColor, fontWeight = FontWeight.SemiBold)
    }
}

