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
        val ERROR_CONNECTION: NetworkState
        val WAITING: NetworkState
        val SUCCESS: NetworkState
        val ERROR_API: NetworkState
        init {
            WAITING = NetworkState(Status.RUNNING, "ns_waiting")
            LOADED = NetworkState(Status.SUCCESS, "ns_success")
            LOADING = NetworkState(Status.RUNNING, "ns_running")
            ERROR = NetworkState(Status.FAILED, "ns_went_wrong")
            ENDOFLIST = NetworkState(Status.FAILED, "ns_end_list")
            NODATA = NetworkState(Status.FAILED, "ns_no_data")
            ERROR_CONNECTION = NetworkState(Status.FAILED, "ns_problem_connection")
            ERROR_API = NetworkState(Status.FAILED, "ns_problem_api")
            SUCCESS = NetworkState(Status.SUCCESS, "ns_success")

        }
    }
}