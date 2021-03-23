package com.eqworks

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


data class LocationEvent(val lat: Double, val lon: Double, val time: String, val ext: String = "empty")

class Library(private val activity: Activity, private val url: String = "https://httpbin.org/post") {

    private val PERMISSION_ID = 1000
    private val LOG_TAG = "EQWORKS-LIBRARY"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    var textView: TextView? = null

    fun setup(): Boolean {
        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        return true
    }

    fun log(event: LocationEvent) {
        // POST to API Server
        postDataToServer(url, getResultString(event.lat, event.lon, event.time))
    }

    fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        log(LocationEvent(location.latitude, location.longitude, getDateTime(), "empty"))
                        getNewLocation()
                    }
                    getNewLocation()
                }
            } else {
                Toast.makeText(activity.applicationContext, "Please Enable Location", Toast.LENGTH_SHORT).show()
                Handler().postDelayed(Runnable {
                    activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }, 1000)
            }
        } else {
            requestPermission()
            if (checkPermission() && isLocationEnabled())
                getLastLocation()
        }
    }

    /**
     * Provides the updated location
     */
    private fun getNewLocation() {
        locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        if (!checkPermission()) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    /**
     * location call back for the new location event i.e. updated location
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            log(LocationEvent(p0?.lastLocation?.latitude ?: 0.0,
                p0?.lastLocation?.longitude ?: 0.0, getDateTime(), "empty"))
        }
    }

    /**
     * checks the permission for the location
     */
    private fun checkPermission(): Boolean {
        return (
                ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                )
    }

    /**
     * Request the location permission
     */
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    /**
     * Returns true or false if the location service is enabled or not
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Event to receive the result of the permission request
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID && grantResults.isNotEmpty()) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
            else {
                log(LocationEvent(0.0, 0.0, getDateTime(), ""))
            }

        }
    }

    /**
     * Makes a location event posting string
     */
    private fun getResultString(lat: Double, lon: Double, dateTime: String): String {
        return "Latitude: $lat, Longitude : $lon, DateTime: $dateTime"
    }

    /**
     * Provides the date and time of the location event triggering
     */
    @SuppressLint("SimpleDateFormat")
    private fun getDateTime(): String {
        return SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time) + ";" +
                SimpleDateFormat("dd-MM-yyy").format(Date())
    }


    /**
     * post the data to the server with the specified url and argument(location event)
     */
    private fun postDataToServer(url: String, args: String) {
        val dialog = AlertDialog.Builder(activity).apply {
            setTitle("Fetching Data")
            setMessage("Please wait while we are fetching data")
            create()
        }.show()

        val queue = Volley.newRequestQueue(activity)
        val postData = JSONObject()
        try {
            postData.put("args", args)
        } catch (e: JSONException) {
            e.stackTrace
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, postData,
            { response ->
                dialog.dismiss()
                textView?.text = "Response is:\n ${parseData(response)}"
                Log.i(LOG_TAG, parseData(response))
            },
            {
                dialog.dismiss()
                textView?.text = "That didn't work"
                Log.i(LOG_TAG, it.localizedMessage.toString())
            }
        )
        queue.add(jsonObjectRequest)
    }

    /**
     * Parses the json result to represent in the log
     */
    private fun parseData(data: JSONObject): String {
        return JSONObject(data.getString("data")).getString("args") ?: "No json data"
    }
}

