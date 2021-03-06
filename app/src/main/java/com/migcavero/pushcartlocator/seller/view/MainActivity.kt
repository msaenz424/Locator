package com.migcavero.pushcartlocator.seller.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.firebase.ui.auth.AuthUI
import com.migcavero.pushcartlocator.seller.BuildConfig
import com.migcavero.pushcartlocator.seller.R
import java.util.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.migcavero.pushcartlocator.seller.presenter.MainPresenterImpl
import android.content.IntentSender
import android.location.Location
import android.location.LocationManager
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.design.longSnackbar

class MainActivity : AppCompatActivity(), MainView,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private val ZOOM_LEVEL = 15
    private val PERMISSION_REQUEST_CODE = 101
    private val SETTINGS_REQUEST_CODE = 102
    private val RC_SIGN_IN = 123

    private lateinit var mMainPresent: MainPresenterImpl
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        visibility_switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkGpsStatus()) {
                    requestLocationSettings()
                }
                connectGoogleApiClient()
                longSnackbar(main_linear_layout, R.string.visibility_on_message)
            } else {
                disconnectGoogleApiClient()
                mMainPresent.onVisibilitySwitchOff()
                longSnackbar(main_linear_layout, R.string.visibility_off_message)
            }
        }

        mMainPresent = MainPresenterImpl(this)
        mMainPresent.onCreate()
        buildGoogleApiClient()
    }

    override fun onResume() {
        super.onResume()
        mMainPresent.onResume()
    }

    override fun onPause() {
        mMainPresent.onPause()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish()
                } else {
                    requestLocationSettings()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK) {
                    mMainPresent.onCreate()
                } else {
                    finish()
                }
            }
            SETTINGS_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    initMap()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        connectGoogleApiClient()
        mGoogleMap.isMyLocationEnabled = true
        mGoogleMap.uiSettings.isMyLocationButtonEnabled = true
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest = createLocationRequest()
        if (mGoogleApiClient.isConnected) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
        if (!visibility_switch.isChecked) {
            visibility_switch.isChecked = true
        }
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    private fun connectGoogleApiClient() {
        if (!mGoogleApiClient.isConnected) {
            mGoogleApiClient.connect()
        }
    }

    private fun disconnectGoogleApiClient() {
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        val latitude = location.latitude
        val longitude = location.longitude
        val coordinate = LatLng(latitude, longitude)
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL.toFloat()))
        mMainPresent.onLocationChanged(location)
    }

    override fun displayLoginMethods() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)  // disables it for debug
                        .setAvailableProviders(
                                Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                        .setTheme(R.style.LoginTheme)
                        .build(),
                RC_SIGN_IN)
    }

    override fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }
        } else {
            requestLocationSettings()
        }
    }

    /**
     * Initiates Google Map. Once it's ready it calls the onMapReady method
     */
    override fun initMap() {
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
            initMap()
        }

        task.addOnFailureListener(this) { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MainActivity, SETTINGS_REQUEST_CODE)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun checkGpsStatus(): Boolean {
        val mLocationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

}
