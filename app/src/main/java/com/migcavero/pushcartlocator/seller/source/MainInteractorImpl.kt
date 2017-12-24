package com.migcavero.pushcartlocator.seller.source

import android.location.Location
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainInteractorImpl: MainInteractor {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mUserID: String
    private val mDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("carts_available")
    private val mGeoFire = GeoFire(mDatabaseReference)

    override fun authenticateUser(onFinishedListener: MainInteractor.OnFinishedListener) {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            mUserID = user?.uid ?: ""
            if (!mUserID.isBlank()) {
                onFinishedListener.onAuthenticationSuccess()
            } else {
                onFinishedListener.onAuthenticationFail()
            }
        }
    }

    override fun addAuthStateListener() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun removeAuthStateListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    override fun updateLocation(location: Location) {
        if (!mUserID.isBlank()) {
            mGeoFire.setLocation(mUserID, GeoLocation(location.latitude, location.longitude))
        }
    }

    override fun removeLocation() {
        if (!mUserID.isBlank()){
            mGeoFire.removeLocation(mUserID)
        }
    }
}