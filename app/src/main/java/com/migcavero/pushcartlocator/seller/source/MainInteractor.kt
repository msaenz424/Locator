package com.migcavero.pushcartlocator.seller.source

import android.location.Location

interface MainInteractor {

    fun authenticateUser(onFinishedListener: OnFinishedListener)

    fun addAuthStateListener()

    fun removeAuthStateListener()

    fun updateLocation(location: Location)

    fun removeLocation()

    interface OnFinishedListener{

        fun onAuthenticationSuccess()

        fun onAuthenticationFail()

    }

}