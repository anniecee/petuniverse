
package com.example.pet_universe.ui.sellerView

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pet_universe.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        // Get existing coordinates if editing
        val existingLat = intent.getDoubleExtra("latitude", 0.0)
        val existingLng = intent.getDoubleExtra("longitude", 0.0)
        if (existingLat != 0.0 && existingLng != 0.0) {
            selectedLocation = LatLng(existingLat, existingLng)
        }

        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize buttons
        confirmButton = findViewById(R.id.confirmLocationButton)
        cancelButton = findViewById(R.id.cancelLocationButton)

        confirmButton.setOnClickListener {
            if (selectedLocation != null) {
                val resultIntent = Intent().apply {
                    putExtra("latitude", selectedLocation!!.latitude)
                    putExtra("longitude", selectedLocation!!.longitude)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set default location to Vancouver
        val defaultLocation = selectedLocation ?: LatLng(49.2827, -123.1207)

        // If there's an existing location, show it
        if (selectedLocation != null) {
            mMap.addMarker(MarkerOptions().position(selectedLocation!!).title("Selected Location"))
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Set up map click listener
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            selectedLocation = latLng
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
        }
    }
}