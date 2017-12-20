package com.migcavero.pushcartlocator.seller.source

import com.google.firebase.auth.FirebaseAuth

class MainInteractorImpl: MainInteractor {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun authenticateUser(onFinishedListener: MainInteractor.OnFinishedListener) {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
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

}