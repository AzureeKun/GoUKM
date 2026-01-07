package com.example.goukm.ui.booking

import android.content.Context
import com.example.goukm.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import com.google.android.gms.maps.model.LatLng

class PlacesRepository(private val context: Context) {
    private val placesClient: PlacesClient

    init {
        // Initialize Places if not already initialized
        if (!Places.isInitialized()) {
            val apiKey = context.getString(R.string.google_maps_key)
            Places.initialize(context.applicationContext, apiKey)
        }
        placesClient = Places.createClient(context)
    }

    suspend fun getPredictions(query: String, biasLocation: LatLng? = null): List<AutocompletePrediction> {
        return try {
            val builder = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("MY") // Limit to Malaysia

            if (biasLocation != null) {
                // Bias results to 5km radius around the user
                val lat = biasLocation.latitude
                val lng = biasLocation.longitude
                val offset = 0.05 // Approx 5km
                
                val sw = LatLng(lat - offset, lng - offset)
                val ne = LatLng(lat + offset, lng + offset)
                val bounds = com.google.android.libraries.places.api.model.RectangularBounds.newInstance(sw, ne)
                
                builder.setLocationBias(bounds)
                builder.setOrigin(biasLocation) 
            }

            val request = builder.build()
            val response = placesClient.findAutocompletePredictions(request).await()
            response.autocompletePredictions
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPlaceDetails(placeId: String): Place? {
        return try {
            val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            val request = FetchPlaceRequest.builder(placeId, placeFields).build()

            val response = placesClient.fetchPlace(request).await()
            response.place
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    data class RouteResult(
        val polyline: List<com.google.android.gms.maps.model.LatLng>,
        val distance: String,
        val duration: String
    )

    suspend fun getRoute(origin: com.google.android.gms.maps.model.LatLng, destination: com.google.android.gms.maps.model.LatLng): Result<RouteResult> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val apiKey = context.getString(R.string.google_maps_key)
                val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${destination.latitude},${destination.longitude}" +
                        "&key=$apiKey"

                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .header("X-Android-Package", context.packageName)
                    .build()
                val response = client.newCall(request).execute()
                val jsonData = response.body?.string()

                if (jsonData != null) {
                    val jsonObject = org.json.JSONObject(jsonData)
                    val status = jsonObject.optString("status")
                    
                    if (status == "OK") {
                        val routes = jsonObject.getJSONArray("routes")
                        if (routes.length() > 0) {
                            val route = routes.getJSONObject(0)
                            
                            // Get Polyline
                            val overviewPolyline = route.getJSONObject("overview_polyline")
                            val points = overviewPolyline.getString("points")
                            
                            // Get Distance & Duration
                            var distanceText = ""
                            var durationText = ""
                            val legs = route.getJSONArray("legs")
                            if (legs.length() > 0) {
                                val leg = legs.getJSONObject(0)
                                distanceText = leg.getJSONObject("distance").getString("text")
                                durationText = leg.getJSONObject("duration").getString("text")
                            }

                            return@withContext Result.success(RouteResult(decodePolyline(points), distanceText, durationText))
                        } else {
                            return@withContext Result.failure(Exception("No routes found"))
                        }
                    } else {
                        val errorMessage = jsonObject.optString("error_message", "Unknown error")
                        return@withContext Result.failure(Exception("$status: $errorMessage"))
                    }
                }
                Result.failure(Exception("Empty response"))
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private fun decodePolyline(encoded: String): List<com.google.android.gms.maps.model.LatLng> {
        val poly = ArrayList<com.google.android.gms.maps.model.LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = com.google.android.gms.maps.model.LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }
        return poly
    }
}
