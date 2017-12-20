package com.migcavero.pushcartlocator.seller.view

import com.google.android.gms.maps.GoogleMap

interface MainView {

    /**
     * Displays the Firebase's default UI login screen
     */
    fun displayLoginMethods()

    /**
     * Ask the user for location permission if it wasn't granted
     */
    fun requestPermission()

    /**
     * Initialize the map widget
     */
    fun displayMap()

    /**
     * Display user's last known location
     */
    fun displayLocation(googleMap: GoogleMap)
}