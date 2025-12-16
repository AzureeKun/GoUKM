package com.example.goukm.ui.booking

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRideOfferScreen() {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    // Dummy Data for visual reproduction
    val pickupPoint = "Kolej Aminuddin Baki"
    val dropOffPoint = "Kolej Pendeta Za'ba"

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 100.dp,
        sheetContainerColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetDragHandle = {}, // Custom handle
        sheetContent = {
            // We use a LazyColumn for the whole sheet content to make it scrollable nicely
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(4.dp)
                                .background(Color.LightGray, RoundedCornerShape(50))
                        )
                    }
                }

                item {
                    // Header
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "4 seater ride",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    // Route Info
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Pickup
                        Column {
                            Text(
                                "Pickup Point",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Black
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(pickupPoint, color = Color.DarkGray)
                                }
                            }
                        }

                        // Dropoff
                        Column {
                            Text(
                                "Drop-Off Point",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Black
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(dropOffPoint, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Text(
                        "Ride Offers",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(driverOffers) { offer ->
                    DriverOfferCard(offer = offer)
                }
                
                // Extra space at bottom
                item { 
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) { innerPadding ->
        // Map Background
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val ukmLocation = LatLng(2.9300, 101.7774)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(ukmLocation, 15f)
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false
                )
            )
        }
    }
}

data class DriverOfferDetail(
    val driverName: String,
    val carBrand: String,
    val carModel: String,
    val carColor: String,
    val plateNumber: String,
    val price: String
)

val driverOffers = listOf(
    DriverOfferDetail("ANGELA KELLY", "Perodua", "Axia", "Silver", "KFM1044", "RM 5"),
    DriverOfferDetail("FARHAN ISKANDAR", "Proton", "Persona", "Blue", "LA 3456", "RM 7"),
    DriverOfferDetail("FATTEH MUSTAFA", "Honda", "Civic", "Red", "VFG 1322", "RM 4")
)

@Composable
fun DriverOfferCard(offer: DriverOfferDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                    )
                }
                Spacer(Modifier.height(4.dp))
                // Split name for visual similarity if needed, or just show it
                val nameParts = offer.driverName.split(" ")
                nameParts.forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            // Price badge
            Box(
                modifier = Modifier
                    .background(Color(0xFF6B87C0), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = offer.price,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(Modifier.width(12.dp))

            // Car Info & Accept Button Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Car Brand: ${offer.carBrand}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "Car Name: ${offer.carModel}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "Car Color: ${offer.carColor}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "Number Plate: ${offer.plateNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { /* Accept */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD60A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Accept",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomerRideOfferScreenPreview() {
    MaterialTheme {
        CustomerRideOfferScreen()
    }
}
