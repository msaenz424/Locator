package com.migcavero.pushcartlocator.seller.source

interface MainInteractor {

    fun authenticateUser(onFinishedListener: OnFinishedListener)

    fun addAuthStateListener()

    fun removeAuthStateListener()

    interface OnFinishedListener{

        fun onAuthenticationSuccess()

        fun onAuthenticationFail()

    }

}