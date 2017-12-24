package com.migcavero.pushcartlocator.seller.presenter

import android.location.Location

interface MainPresenter {

    fun onCreate()

    fun onResume()

    fun onPause()

    fun onStop()

    fun onLocationChanged(location: Location)

}