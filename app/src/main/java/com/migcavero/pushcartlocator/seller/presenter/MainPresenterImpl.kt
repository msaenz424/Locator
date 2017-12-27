package com.migcavero.pushcartlocator.seller.presenter

import android.location.Location
import com.migcavero.pushcartlocator.seller.source.MainInteractor
import com.migcavero.pushcartlocator.seller.source.MainInteractorImpl
import com.migcavero.pushcartlocator.seller.view.MainView

class MainPresenterImpl constructor(mainView: MainView) : MainPresenter, MainInteractor.OnFinishedListener {

    val mMainView = mainView
    val mMainInteractor = MainInteractorImpl()

    override fun onCreate() {
        mMainInteractor.authenticateUser(this)
    }

    override fun onResume() {
        mMainInteractor.addAuthStateListener()
    }

    override fun onPause() {
        mMainInteractor.removeAuthStateListener()
    }

    override fun onLocationChanged(location: Location) {
        mMainInteractor.updateLocation(location)
    }

    override fun onVisibilitySwitchOff() {
        mMainInteractor.removeLocation()
    }

    override fun onAuthenticationSuccess() {
        mMainView.requestPermission()
    }

    override fun onAuthenticationFail() {
        mMainView.displayLoginMethods()
    }

}