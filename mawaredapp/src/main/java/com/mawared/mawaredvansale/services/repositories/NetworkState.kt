package com.mawared.mawaredvansale.services.repositories

enum class Status{
    RUNNING,
    SUCCESS,
    FAILED
}
class NetworkState(val status: Status, val msg: String) {

    companion object{
        val LOADED: NetworkState
        val LOADING: NetworkState
        val ERROR: NetworkState
        val ENDOFLIST: NetworkState
        val NODATA: NetworkState

        init {
            LOADED = NetworkState(Status.SUCCESS, "ns_success")
            LOADING = NetworkState(Status.RUNNING, "ns_unning")
            ERROR = NetworkState(Status.FAILED, "ns_went_wrong")
            ENDOFLIST = NetworkState(Status.FAILED, "ns_end_list")
            NODATA = NetworkState(Status.FAILED, "ns_no_data")
        }
    }
}