package com.mawared.mawaredvansale.controller.map

import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.utilities.EXTRA_CURRENT_LOCATION
import com.mawared.mawaredvansale.utilities.EXTRA_DATA_LOCATIONS
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private val TAG = MapsActivity::class.java.simpleName
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    private lateinit var mLastLocation: Location
    private var mMarker: Marker? = null

    // location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    private var locations: ArrayList<LocationsData>? = null
    private var location: Location? = null

    companion object{
        private const val MY_PERMISSION_CODE: Int = 1000
        //private const val ACCESS_FINE_LOCATION_CODE = 120
    }
    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val bundle = intent.extras
        if (bundle != null) {
            val ld = bundle.getSerializable(EXTRA_DATA_LOCATIONS)
            if (ld != null) {
                locations = ld as ArrayList<LocationsData>
            }
            val l = bundle.getParcelable<Location>(EXTRA_CURRENT_LOCATION)
            if (l != null) {
                location = l //as LocationsData
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
//        mapFragment.getMapAsync(OnMapReadyCallback {
//            googleMap = it
//            googleMap.isMyLocationEnabled = true
//            val loc1 = LatLng(location!!.latitude, location!!.longitude)
//            googleMap.addMarker(MarkerOptions().position(loc1).title("Current Location"))
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc1, 10f))
//        })

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocationPermission()
        }
        buildLocationRequest()
        buildLocationCallBack()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
//        if (!MapUtils.isConnectedToInternet(this)) {
//            Toast.makeText(this, "Please connect to internet!", Toast.LENGTH_LONG).show()
//        }
//
//        if (!MapUtils.isLocationServiceEnabled(this)) {
//            Toast.makeText(this, "Please enable location services!", Toast.LENGTH_LONG).show()
//        }
//        val permissionGranted = ActivityCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) === PackageManager.PERMISSION_GRANTED
//
//        if (permissionGranted) {
//            Log.i(TAG, "permissionGranted fine location")
//        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                ACCESS_FINE_LOCATION_CODE
//            )
//        }
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                mLastLocation = p0!!.locations.get(p0.locations.size-1) // Get last location

                if(mMarker != null){
                    mMarker!!.remove()
                }

                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude
                val latLng = LatLng(latitude, longitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("Your Position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = googleMap.addMarker(markerOptions)

                // Move camera
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                googleMap.animateCamera(CameraUpdateFactory.zoomBy(11f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission(): Boolean {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            }
            return false
        }
        return true
    }

    //Override OnRequestPermissionsResult
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        when(requestCode){
            MY_PERMISSION_CODE ->{
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(checkLocationPermission()){
                            googleMap.isMyLocationEnabled = true
                        }
                    }
                } else{
                    rl_map.snackbar("Permission Denied")
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap){
        this.googleMap = googleMap

        // Init Google play Services
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.isMyLocationEnabled = true
            }
        }else{
            googleMap.isMyLocationEnabled = true
        }

        // Enable zoom control
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

}
