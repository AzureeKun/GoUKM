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

class PlacesRepository(context: Context) {
    private val placesClient: PlacesClient

    init {
        // Initialize Places if not already initialized
        if (!Places.isInitialized()) {
            val apiKey = context.getString(R.string.google_maps_key)
            Places.initialize(context.applicationContext, apiKey)
        }
        placesClient = Places.createClient(context)
    }

    suspend fun getPredictions(query: String): List<AutocompletePrediction> {
        return try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("MY") // Limit to Malaysia
                .build()

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
}
