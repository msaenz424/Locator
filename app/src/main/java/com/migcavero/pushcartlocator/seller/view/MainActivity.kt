package com.migcavero.pushcartlocator.seller.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.migcavero.pushcartlocator.seller.BuildConfig
import com.migcavero.pushcartlocator.seller.R
import java.util.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.migcavero.pushcartlocator.seller.presenter.MainPresenterImpl
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationRequest
import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException

class MainActivity : AppCompatActivity(), MainView, OnMapReadyCallback {

    private val ZOOM_LEVEL = 12
    private val ZOOM_DURATION = 2000
    private val PERMISSION_REQUEST_CODE = 101
    private val SETTINGS_REQUEST_CODE = 102
    private val RC_SIGN_IN = 123

    private lateinit var mMainPresent: MainPresenterImpl
    private lateinit var mGoogleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMainPresent = MainPresenterImpl(this)
        mMainPresent.onCreate()
    }

    override fun onResume() {
        super.onResume()
        mMainPresent.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMainPresent.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish()
                } else {
                    Log.d("onRequestPermissionsRes", "permission granted, calling initMap")
                    initMap()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SETTINGS_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d("onActivityResult", "resultCode = OK, calling initMap")
                    initMap()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("onMapReady", "map ready, calling displayLocation")
        mGoogleMap = googleMap
        displayLocation()
    }

    override fun displayLoginMethods() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)  // disables it for debug
                        .setAvailableProviders(
                                Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                        .build(),
                RC_SIGN_IN)
    }

    override fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
                Log.d("requestPermission", "permission not granted")
            }
        } else {
            Log.d("requestPermission", "permission granted, calling requestLocationSettings")
            requestLocationSettings()
        }
    }

    override fun displayLocation() {
        val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            mGoogleMap.isMyLocationEnabled = true
            mGoogleMap.uiSettings.isMyLocationButtonEnabled = true
            mFusedLocationClient.lastLocation
                    .addOnSuccessListener(this) { location ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("displayLocation", "location not null")
                            // Logic to handle location object
                            val latitude = location.latitude
                            val longitude = location.longitude
                            val coordinate = LatLng(latitude, longitude)
                            mGoogleMap.addMarker(MarkerOptions().position(coordinate))
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
                            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL.toFloat()), ZOOM_DURATION, null)
                        } else {
                            /** TODO needs to be improved. Location is null the first time map is initialized 
                             *  initMap shouldn't be necessary here
                             * */
                            initMap()
                            Log.d("displayLocation", "location = null")
                        }
                    }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    /**
     * Initiates Google Map. Once it's ready it calls the onMapReady method
     */
    override fun initMap() {
        Log.d("initMap", "initializing map")
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    private fun requestLocationSettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(createLocationRequest())

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener(this) {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            Log.d("requestLocationSettings", "task success")
            initMap()
        }

        task.addOnFailureListener(this) { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    Log.d("requestLocationSettings", "task fail, calling startResolutionForResult")
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MainActivity, SETTINGS_REQUEST_CODE)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

}
