package com.migcavero.pushcartlocator.seller.view

import android.Manifest
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

class MainActivity : AppCompatActivity(), MainView, OnMapReadyCallback {

    private val ZOOM_LEVEL = 12
    private val ZOOM_DURATION = 2000
    private val PERMISSION_REQUEST_CODE = 101
    private val RC_SIGN_IN = 123

    private lateinit var mMainPresent: MainPresenterImpl

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
                    initMap()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission == PackageManager.PERMISSION_GRANTED) {
            displayLocation(googleMap)
        } else {
            requestPermission()
        }
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
            }
        } else {
            initMap()
        }
    }

    override fun displayMap() {
        initMap()
    }

    override fun displayLocation(googleMap: GoogleMap) {
        val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            mFusedLocationClient.lastLocation
                    .addOnSuccessListener(this) { location ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            val latitude = location.latitude
                            val longitude = location.longitude
                            val coordinate = LatLng(latitude, longitude)
                            googleMap.addMarker(MarkerOptions().position(coordinate))
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
                            googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL.toFloat()), ZOOM_DURATION, null)
                        }
                    }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    /**
     * Initiates Google Map. Once it's ready it calls the onMapReady method
     */
    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
}
