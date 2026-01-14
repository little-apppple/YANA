package com.example.stillalive.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

import kotlinx.coroutines.withTimeout

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context, timeoutMs: Long = 5000L): String {
        if (!hasLocationPermission(context)) {
            return "无定位权限"
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // 1. Get last known location as fallback
        val lastKnownLocation = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) 
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            null
        }

        // 2. Try to get fresh location with timeout
        return try {
            withTimeout(timeoutMs) {
                suspendCancellableCoroutine { cont ->
                    val locationListener = object : android.location.LocationListener {
                        override fun onLocationChanged(location: Location) {
                            if (cont.isActive) {
                                cont.resume(formatLocation(location))
                                try { locationManager.removeUpdates(this) } catch (e: Exception) {}
                            }
                        }
                        override fun onProviderDisabled(provider: String) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                    }
                    
                    cont.invokeOnCancellation {
                        try { locationManager.removeUpdates(locationListener) } catch (e: Exception) {}
                    }
                    
                    try {
                        var requestStarted = false
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper())
                            requestStarted = true
                        } 
                        
                        // Request Network as well if GPS didn't start or just to be sure (though usually one is enough for single update, 
                        // requesting both might lead to race conditions or faster result. standard is usually one. 
                        // let's stick to priority: GPS -> Network if GPS not enabled)
                        // Or better: request Network if GPS not enabled.
                        // Re-reading original code: it used 'else if'. 
                        
                        if (!requestStarted && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.getMainLooper())
                            requestStarted = true
                        }
                        
                        if (!requestStarted) {
                            cont.resume("定位服务未开启")
                        }
                    } catch (e: Exception) {
                        if (cont.isActive) cont.resumeWith(Result.failure(e))
                    }
                }
            }
        } catch (e: Exception) {
            // Timeout or error: Fallback to last known
            if (lastKnownLocation != null) {
                formatLocation(lastKnownLocation) + " (历史)"
            } else {
                "无法获取定位(超时/无记录)"
            }
        }
    }

    private fun formatLocation(location: Location): String {
        return "Lat:${location.latitude}, Lon:${location.longitude}"
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
