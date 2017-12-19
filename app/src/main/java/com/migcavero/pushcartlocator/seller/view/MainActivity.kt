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
import com.google.firebase.auth.FirebaseAuth
import com.migcavero.pushcartlocator.seller.BuildConfig
import com.migcavero.pushcartlocator.seller.R
import java.util.*
import com.google.android.gms.maps.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val PERMISSION_REQUEST_CODE = 101
    private val RC_SIGN_IN = 123
    private val ZOOM_LEVEL = 12
    private val ZOOM_DURATION = 2000

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private var mMap: GoogleMap? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // already signed in
                requestPermission()
            } else {
                // not signed in
                displayLoginMethods()
            }
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
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

    override fun onMapReady(map: GoogleMap?) {
        mMap = map

        if (mMap != null) {
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

            if (permission == PackageManager.PERMISSION_GRANTED) {
                updateLocationUI()
            } else {
                requestPermission()
            }
        }
    }

    /**
     * Displays the Firebase's default UI login screen
     */
    fun displayLoginMethods() {
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

    /**
     * Ask the user for location permission if it wasn't granted
     */
    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }
        } else {
            initMap()
        }
    }

    /**
     * Initiates Google Map. Once it's ready it calls the onMapReady method
     */
    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Display the user's last know location
     */
    private fun updateLocationUI() {
        if (mMap != null) {
            try {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
                mFusedLocationClient!!.lastLocation
                        .addOnSuccessListener(this) { location ->
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                val latitude = location.latitude
                                val longitude = location.longitude
                                val coordinate = LatLng(latitude, longitude)
                                mMap!!.addMarker(MarkerOptions().position(coordinate))
                                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
                                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL.toFloat()), ZOOM_DURATION, null)
                            }
                        }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message)
            }
        }
    }
}
