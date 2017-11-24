package com.migcavero.pushcartlocator.seller.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.migcavero.pushcartlocator.seller.BuildConfig
import com.migcavero.pushcartlocator.seller.R
import java.util.*

class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 123

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // already signed in

            } else {
                // not signed in
                displayLoginMethods()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
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

}
