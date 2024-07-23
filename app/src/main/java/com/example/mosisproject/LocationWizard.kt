package com.example.mosisproject

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

import java.util.*
import java.util.concurrent.TimeUnit



@SuppressLint("MissingPermission")
class LocationWizard {
    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private var locationByFused: Location? = null

    private var hasGps = false
    private var hasNetwork = false

    private val tag = "TAG_LOCATION_WIZARD"

    //Fused
    private var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MOSISProject.applicationContext())
    private var locationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(10)
        fastestInterval = 4000L
        maxWaitTime = TimeUnit.MINUTES.toMillis(10)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let {
                locationByFused = it

            }
        }
    }

    private val locationManager: LocationManager =
        MOSISProject.applicationContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val handler: Handler = Handler()
    private var locationChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                updateLocation()
            } finally {
                handler.postDelayed(this, 4000L)
            }
        }
    }

    private val gpsLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationByGps = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private val networkLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationByNetwork = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    init {
        try {
            hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (hasGps) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    gpsLocationListener
                )
            }
            if (hasNetwork) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0F,
                    networkLocationListener
                )
            }

            locationChecker.run()
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        } catch (e: Exception) {
            Log.d(tag, "init locationWizard $e")
        }
    }

    fun updateLocation() {
        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {
            locationByGps = lastKnownLocationByGps
        }

        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let {
            locationByNetwork = lastKnownLocationByNetwork
        }
    }

    fun findMostAccurateLocation(): Location? {
        val finalLocation = locationByGps ?: locationByFused ?: locationByNetwork
        //ovo ovde assignuje trenutni naziv(based on coordinates) lokacije u provider
        finalLocation?.provider = getAddress(finalLocation?.latitude ?: 0.0, finalLocation?.longitude ?: 0.0)

        return finalLocation
    }

    private fun getAddress(lat: Double, long: Double): String {
        try {
            val geoCoder = Geocoder(MOSISProject.applicationContext(), Locale.getDefault())
            val address = geoCoder.getFromLocation(lat, long, 3)

            address?.let {
                if (it.isNotEmpty() && it[0] != null) return it[0].getAddressLine(0)
            }
        } catch (e: Exception) {
            return ""
        }

        return ""
    }

    fun stopLocationChecker() {
        handler.removeCallbacks(locationChecker)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mInstance: LocationWizard? = null
        val instance: LocationWizard?
            get() {
                if (mInstance == null) mInstance = LocationWizard()
                return mInstance
            }
    }
}