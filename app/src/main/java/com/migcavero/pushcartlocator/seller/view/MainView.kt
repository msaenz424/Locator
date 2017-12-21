package com.migcavero.pushcartlocator.seller.view

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
    fun initMap()

    /**
     * Display user's last known location
     */
    fun displayLocation()
}