package com.mawared.mawaredvansale.controller.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.databinding.MapFragmentBinding
import com.mawared.mawaredvansale.interfaces.IDatePicker
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import java.util.*


class MapFragment : Fragment(), KodeinAware, IMainNavigator<Call_Cycle>, IDatePicker, GoogleMap.OnMarkerClickListener {
    //Now we need to create some varaibales that we will need
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    var vanMarker: Marker? = null
    var AllMarkers = arrayListOf<Marker>()
    //private var manager: LocationManager?
    // the permission id is just an int that must be unique so you can use any number
    private var PERMISSION_ID = 52
    private val LOCATION_PERMISSION_REQUEST = 1

    override val kodein by kodein()

    private lateinit var mMap: GoogleMap
    private var mapReady = false

    private val factory: MapViewModelFactory by instance()

    val viewModel by lazy {  ViewModelProviders.of(this, factory).get(MapViewModel::class.java) }
    private lateinit var binding: MapFragmentBinding

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.map_fragment, container, false)
        //val rootView = inflater.inflate(R.layout.map_fragment, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment

        mapFragment.getMapAsync{ googleMap -> mMap = googleMap
            mapReady = true
            updateMap()
            getLocationAccess()
            mMap.setOnMarkerClickListener(this)
           // mMap.setOnMarkerClickListener(onMarkerClick)
        }

        viewModel.dateNavigator = this
        viewModel.navigator = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun onResume() {
        removeObservers()
        super.onResume()
    }

    override fun onStop() {
        removeObservers()
        super.onStop()
    }

    private fun removeObservers(){
        viewModel.customerList.removeObservers(this)
        viewModel.selectedCustomer.removeObservers(viewLifecycleOwner)
        viewModel.salesmanSum.removeObservers(viewLifecycleOwner)
        //viewModel.networkStateRV.removeObservers(this)
    }

    override fun onDestroyView() {
        removeObservers()
        onDestroy()
        super.onDestroyView()
    }

    /** Called when the user clicks a marker.  */
    override fun onMarkerClick(marker: Marker): Boolean {

        // Retrieve the data from the marker.
        val cu_Id = marker.tag as? Int

        if(cu_Id != 0 && cu_Id != null){
            viewModel.cu_ref_Id.value = cu_Id!!
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Now let's intiate the fused..providerclient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        //getLastLocation()

    }

    private fun bindUI() = GlobalScope.launch(Main){
        viewModel.customerList.observe(viewLifecycleOwner, Observer { cu ->
            viewModel.places = cu
            updateMap()
        })

        viewModel.selectedCustomer.observe(viewLifecycleOwner, Observer {
            if(it != null){
                viewModel.cuName.value = it.cu_name_ar ?: ""
                viewModel.cuPhone.value = it.cu_phone ?: ""
                viewModel.cuVisit.value = it.cu_last_visit_name ?: ""
                viewModel.cuVisitDate.value = it.cu_last_visit_date ?: ""
                viewModel.cuBalance.value = it.cu_balance ?: 0.0
                viewModel.cu_Id = it.cu_ref_Id
                viewModel.cu_dayname = it.cu_dayname
                viewModel.cu_isVisited.value = if(it.cu_isVisited.isNullOrEmpty() || it.cu_isVisited == "Y") "N" else "Y"
            }
        })

        viewModel.salesmanSum.observe(viewLifecycleOwner, Observer {
            if(it != null){
                viewModel.totCust.value = it.totCust ?: 0
                viewModel.totVisCust.value = it.totVisited ?: 0
                viewModel.totUVisCust.value = it.totUnvisit ?: 0
            }
        })
        viewModel.dtSelected.value = viewModel.returnDateString(LocalDate.now().toString())
    }



    private fun updateMap()
    {
        try {
            if(mapReady && viewModel.places != null){
                mMap.clear()
                var loc: LatLng? = null
                viewModel.places!!.forEach{ place ->
                    if(place.cu_latitude != null && place.cu_longitude != null) {
                        val marker = LatLng(
                            place.cu_latitude!!.toDouble(),
                            place.cu_longitude!!.toDouble()
                        )
                        if(place.cu_visit_code == "V"){
                            val m = mMap.addMarker(MarkerOptions().position(marker).title(place.cu_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cu_g)))
                            m.tag = place.cu_Id
                            AllMarkers.add(m)
                        }else{
                            val m = mMap.addMarker(MarkerOptions().position(marker).title(place.cu_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cu_unvisite)))
                            m.tag = place.cu_Id
                            AllMarkers.add(m)
                        }

                        loc = marker;

                    }
                }
//                val zoomLevel = 11.0f //This goes up to 21
//                ///currentLocation = getUserLocation()
//                if(currentLocation != null) {
//                    val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
//                    mMap.addMarker(MarkerOptions().position(latLng).title("Me")).showInfoWindow()
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
//                }else if(loc != null){
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel))
//                }
                //mMap.animateCamera(CameraUpdateFactory.zoomBy(11f))
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //Now we will create a function that will allow us to get the last location
    @SuppressLint("MissingPermission")
    private fun getLastLocation(){
        // first we check permission
        if(CheckPermission()){
            mMap.isMyLocationEnabled = true
            getLocationUpdates()
            startLocationUpdates()
            //Now we check the location service is enabled
//            if(isLocationEnabled()){
//                //Now let's get the Location
//                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
//                    val location: Location? = task.result
//                    if(location == null){
//                        //if the location is null we will get the new user location
//                        //so we need to create a new funtion
//                        //don't forget to add the new location function
//                        getNewLocation()
//
//                    }else{
//                        //location.latitude will return the latitude coordinates
//                        //location.longitude will return the longitude coordinates
//                        currentLocation = location
//                    }
//                }
//            }else{
//                Toast.makeText(
//                    requireContext(),
//                    "Please Enable your Location service",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        }else{
            RequestPermission()
        }
    }

    private fun getLocationUpdates(){
        locationRequest = LocationRequest()
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 20000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval =0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()!!
            //Now let's create the locationCallback variable
        )
    }

    private val locationCallback = object  : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            if(p0.locations.isNotEmpty()) {
                val lastLocation: Location = p0.lastLocation
                if(lastLocation != null) {
                    val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                    if(vanMarker != null){
                        vanMarker!!.remove()
                    }
                    val markerOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_van_c))
                    vanMarker = mMap.addMarker(markerOptions)
                    vanMarker!!.tag = 0;
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        }
    }

    // First we need to create a function that will check the uses permission
    private fun CheckPermission():Boolean{
        if(ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ){
            return true
        }

        return false
    }

    // Now we need to create a funciton that will allow us to get user permission
    private fun RequestPermission(){
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), PERMISSION_ID
        )
    }

    //function to get the city name
    private fun getCityName(lat: Double, long: Double): String{
        var CityName = ""
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, long, 1)

        CityName = address?.get(0)?.locality ?: ""
        return CityName
    }

    //function to get the country name
    private fun getCountryName(lat: Double, long: Double): String{
        var countryName = ""
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, long, 1)

        countryName = address?.get(0)?.countryName ?: ""
        return countryName
    }

    // Now we need a function that check if the location service of the device is enabled
    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLocationAccess() {
        if (CheckPermission()) {
            mMap.isMyLocationEnabled = true
            getLocationUpdates()
            startLocationUpdates()
        }
        else
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // this is a built-in function that check the permission result
        // we will use it just for debugging our code
        if(requestCode == PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocationAccess()
                Log.d("Debag", "You have the permission")
            }else{
                Toast.makeText(requireActivity(), "User has not granted location access permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun ShowDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->
            viewModel.dtSelected.value = "${yr}-${monthOfYear + 1}-${dayOfMonth}"
        }, year, month, day)
        dpd.show()
    }

    override fun onItemEditClick(baseEo: Call_Cycle) {

        val action = MapFragmentDirections.actionMapFragmentToCallCycleEntryFragment()
        action.baseBO = baseEo

        navController.navigate(action)
    }

    override fun onItemDeleteClick(baseEo: Call_Cycle) {

    }

    override fun onItemViewClick(baseEo: Call_Cycle) {

    }

}