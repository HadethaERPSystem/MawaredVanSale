package com.mawared.mawaredvansale.controller.map

import android.location.Location
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.sales.Sale

class LocationsData {
    var location: LatLng? = null
    var bitmapDescriptor: BitmapDescriptor? = null
    var title: String? = null

    /**
     * getting hard coded data, data source can be anything ie. network, db etc.
     *
     * @return
     */

    fun getData(): ArrayList<LocationsData> {
        //hard coded data, can be change dynamically
        val first = LatLng(28.6164, 77.3725)
        val second = LatLng(28.5672, 77.3261)
        val third = LatLng(28.4649, 77.5113)
        val fourth = LatLng(28.5665, 77.3406)


        val bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.marker)
        val bitmapDescriptor2 = BitmapDescriptorFactory.fromResource(R.mipmap.marker2)

        val title = "Fortis Hospital, Noida, Uttar Pradesh, India"
        val title2 = "The Great India Place Mall, Noida, Uttar Pradesh, India"
        val title3 = "Pari Chowk, NRI City, Greater Noida, Uttar Pradesh 201310, India"
        val title4 = "Arun Vihar, Sector 37, Noida, Uttar Pradesh 201303, India"

        val datas = java.util.ArrayList<LocationsData>()

        var data = LocationsData()
        data.location = first
        data.bitmapDescriptor = bitmapDescriptor
        data.title = title
        datas.add(data)

        data = LocationsData()
        data.location = second
        data.bitmapDescriptor = bitmapDescriptor2
        data.title = title2
        datas.add(data)

        data = LocationsData()
        data.location = third
        data.bitmapDescriptor = bitmapDescriptor2
        data.title = title3
        datas.add(data)

        data = LocationsData()
        data.location = fourth
        data.bitmapDescriptor = bitmapDescriptor
        data.title = title4
        datas.add(data)

        return datas
    }

    fun getCurrentLocation(loc: Location): ArrayList<LocationsData>{
        val dts : ArrayList<LocationsData> = arrayListOf()
        val bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.marker)
        val dt = LocationsData()
        dt.location = LatLng(loc.latitude, loc.longitude)
        dt.bitmapDescriptor = bitmapDescriptor
        dts.add(dt)
        return dts
    }

    fun getCallCycleData(baseEoList: List<Call_Cycle>): ArrayList<LocationsData> {
        val data: ArrayList<LocationsData> = arrayListOf()
        for (d: Call_Cycle in baseEoList) {
            if(d.cy_latitude != null && d.cy_longitude != null){
                val da = LocationsData()
                da.location = LatLng(d.cy_latitude!!, d.cy_longitude!!)
                da.bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.marker)
                da.title = d.cy_route_name
                data.add(da)
            }
        }
        return data
    }

    fun getCustomerLocation(baseEoList: List<Customer>): ArrayList<LocationsData>{
        val data: ArrayList<LocationsData> = arrayListOf()
        for (d: Customer in baseEoList) {
            if(d.cu_latitude != null && d.cu_longitude != null){
                val da = LocationsData()
                da.location = LatLng(d.cu_latitude!!, d.cu_longitude!!)
                da.bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.marker)
                da.title = d.cu_rg_name
                data.add(da)
            }
        }
        return data
    }

    fun getInvoicesLocation(baseEoList: List<Sale>): ArrayList<LocationsData>{
        val data: ArrayList<LocationsData> = arrayListOf()
        for (d: Sale in baseEoList) {
            if(d.sl_latitude != null && d.sl_longitude != null){
                val da = LocationsData()
                da.location = LatLng(d.sl_latitude!!, d.sl_longitude!!)
                da.bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.marker)
                da.title = d.sl_region_name
                data.add(da)
            }
        }
        return data
    }
}