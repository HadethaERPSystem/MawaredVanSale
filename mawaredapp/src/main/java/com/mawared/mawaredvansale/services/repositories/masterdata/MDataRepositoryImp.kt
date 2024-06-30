package com.mawared.mawaredvansale.services.repositories.masterdata

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatus
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.masterdata.ItemDS.*

import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import com.mawared.mawaredvansale.utilities.URL_GET_IMAGE
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.threeten.bp.LocalDate
import com.mawared.mawaredvansale.R

import android.graphics.Bitmap
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDoc
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDocLines


private val MINIMUM_INTERVAL = 6

class MDataRepositoryImp(private val api: ApiService, private val db: AppDatabase): IMDataRepository, SafeApiRequest() {

    var job: CompletableJob? = null
//    lateinit var customerPagedList: LiveData<PagedList<Customer>>
//    lateinit var customerDSFactory: CustomerDataSourceFactory
//    lateinit var schCustDSFactory: ScheduledCustomerDataSourceFactory

    //////////////////////
    //// For Product
//    lateinit var itemPagedList: LiveData<PagedList<Product>>
//    lateinit var itemDSFactory: ItemDataSourceFactory
//    lateinit var itemUserDSFactory: ItemOnUserDataSourceFactory
//    lateinit var itemWareDSFactory: ItemOnWareDataSourceFactory

//    override fun fetchCustomerOnPages(
//        sm_Id: Int?,
//        org_Id: Int?,
//        term: String
//    ): LiveData<PagedList<Customer>> {
//        customerDSFactory = CustomerDataSourceFactory(api, sm_Id, org_Id, term)
//        //val ns = Transformations.switchMap<CustomerDataSource, NetworkState>(customerDSFactory.customerLiveDS, CustomerDataSource::networkState)
//        //_networkState.postValue(ns.value)
//
//        val config: PagedList.Config = PagedList.Config.Builder()
//            .setEnablePlaceholders(false)
//            .setPageSize(POST_PER_PAGE)
//            .build()
//
//        customerPagedList = LivePagedListBuilder(customerDSFactory, config).build()
//
//        return customerPagedList
//    }
//
//    override fun getCustomerNetworkState(): LiveData<NetworkState> {
//        return Transformations.switchMap<CustomerDataSource, NetworkState>(
//            customerDSFactory.customerLiveDS,
//            CustomerDataSource::networkState
//        )
//    }
//
//    override fun fetchScheduledCustomerOnPages(sm_Id: Int): LiveData<PagedList<Customer>> {
//        schCustDSFactory = ScheduledCustomerDataSourceFactory(api, sm_Id)
//        val ns = Transformations.switchMap<ScheduledCustomerDataSource, NetworkState>(
//            schCustDSFactory.schCustomerLiveDS,
//            ScheduledCustomerDataSource::networkState
//        )
//        _networkState.postValue(ns.value)
//
//        val config: PagedList.Config = PagedList.Config.Builder()
//            .setEnablePlaceholders(false)
//            .setPageSize(POST_PER_PAGE)
//            .build()
//
//        customerPagedList = LivePagedListBuilder(schCustDSFactory, config).build()
//
//        return customerPagedList
//    }
//
//    override fun getScheduleCustomerNetworkState(): LiveData<NetworkState> {
//        return Transformations.switchMap<ScheduledCustomerDataSource, NetworkState>(
//            schCustDSFactory.schCustomerLiveDS,
//            ScheduledCustomerDataSource::networkState
//        )
//    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////// Product Pages Function
//    override fun fetchItemsOnPages(term: String, priceCode: String): LiveData<PagedList<Product>> {
//        itemDSFactory = ItemDataSourceFactory(api, term, priceCode)
//        val ns = Transformations.switchMap<ItemDataSource, NetworkState>(
//            itemDSFactory.itemLiveDataSource,
//            ItemDataSource::networkState
//        )
//        _networkState.postValue(ns.value)
//
//        val config: PagedList.Config = PagedList.Config.Builder()
//            .setEnablePlaceholders(false)
//            .setPageSize(POST_PER_PAGE)
//            .build()
//
//        itemPagedList = LivePagedListBuilder(itemDSFactory, config).build()
//
//        return itemPagedList
//    }
//
//    override fun getItemNetworkState(): LiveData<NetworkState> {
//        return Transformations.switchMap<ItemDataSource, NetworkState>(
//            itemDSFactory.itemLiveDataSource,
//            ItemDataSource::networkState
//        )
//    }
//
//    override fun fetchItemsByUserOnPages(
//        term: String,
//        userId: Int,
//        priceCode: String
//    ): LiveData<PagedList<Product>> {
//        itemUserDSFactory = ItemOnUserDataSourceFactory(api, term, userId, priceCode)
//        val ns = Transformations.switchMap<ItemOnUserDataSource, NetworkState>(
//            itemUserDSFactory.itemLiveDataSource,
//            ItemOnUserDataSource::networkState
//        )
//        _networkState.postValue(ns.value)
//
//        val config: PagedList.Config = PagedList.Config.Builder()
//            .setEnablePlaceholders(false)
//            .setPageSize(POST_PER_PAGE)
//            .build()
//
//        itemPagedList = LivePagedListBuilder(itemUserDSFactory, config).build()
//
//        return itemPagedList
//    }
//
//    override fun getItemUserNetworkState(): LiveData<NetworkState> {
//        return Transformations.switchMap<ItemOnUserDataSource, NetworkState>(
//            itemUserDSFactory.itemLiveDataSource,
//            ItemOnUserDataSource::networkState
//        )
//    }
//
//    override fun fetchItemsByWarehouseOnPages(
//        term: String,
//        wr_Id: Int,
//        priceCode: String
//    ): LiveData<PagedList<Product>> {
//        itemWareDSFactory = ItemOnWareDataSourceFactory(api, term, wr_Id, priceCode)
//        Transformations.switchMap<ItemOnWareDataSource, NetworkState>(
//            itemWareDSFactory.itemLiveDataSource,
//            ItemOnWareDataSource::networkState
//        ).let {
//            _networkState.postValue(it.value)
//        }
//
//        val config: PagedList.Config = PagedList.Config.Builder()
//            .setEnablePlaceholders(false)
//            .setPageSize(POST_PER_PAGE)
//            .build()
//
//        itemPagedList = LivePagedListBuilder(itemWareDSFactory, config).build()
//
//        return itemPagedList
//    }
//
//    override fun getItemWareNetworkState(): LiveData<NetworkState> {
//        return Transformations.switchMap<ItemOnWareDataSource, NetworkState>(
//            itemWareDSFactory.itemLiveDataSource,
//            ItemOnWareDataSource::networkState
//        )
//    }

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override fun getCustomers(sm_Id: Int, term: String): LiveData<List<Customer>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Customer>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getAllCustomers(sm_Id, term) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        } catch (e: ApiException) {
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        } catch (e: Exception) {
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getCustomers", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun getCustomersOnPages(sm_Id: Int, org_Id: Int?,  term: String,  page: Int): List<Customer>? {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response =
                apiRequest { api.customers_OnPages(sm_Id, org_Id, term, page, POST_PER_PAGE) }
            if (response.isSuccessful) {
                _networkState.postValue(NetworkState.LOADED)
                return response.data
            }
            _networkState.postValue(NetworkState.LOADED)
            return null
        } catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return null
        } catch (e: Exception){
            _networkState.postValue(NetworkState.ERROR)
            Log.e("Error Exception", "${e.message}", e)
            return null
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Error API Exception", "${e.message}", e)
            return null
        }
    }

    override suspend fun getScheduleCustomersOnPages(sm_Id: Int, term: String,  page: Int): List<Customer>? {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response =
                apiRequest { api.customers_getScheduleOnPages(sm_Id, term, page, POST_PER_PAGE) }
            if (response.isSuccessful) {
                _networkState.postValue(NetworkState.LOADED)
                return response.data
            }
            _networkState.postValue(NetworkState.LOADED)
            return null
        } catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return null
        } catch (e: Exception){
            _networkState.postValue(NetworkState.ERROR)
            Log.e("Error Exception", "${e.message}", e)
            return null
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Error API Exception", "${e.message}", e)
            return null
        }
    }

    override fun getCustomers_ByTerm(sm_Id: Int,term: String, mntTypeCode: String): LiveData<List<Customer>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Customer>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getCustomers_ByTerm(sm_Id, term, mntTypeCode) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getCustomers_ByTerm", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getCustomerStatus(cu_Id: Int): LiveData<CustomerStatus> {
        job = Job()
        return object : LiveData<CustomerStatus>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.customers_getStatus(cu_Id) }
                            withContext(Main) {
                                value = response.data
                                //_networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            //_networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            //_networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call customers schedule", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun customers_getSchedule(sm_Id: Int, term: String): LiveData<List<Customer>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Customer>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.customers_getSchedule(sm_Id, term) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call customers schedule", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun customers_getPlaces(sm_Id: Int, cyDate: String): LiveData<List<Customer>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Customer>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.customers_getPlaces(sm_Id, cyDate) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call customers Places", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getCustomersByOrg(sm_id: Int, org_Id: Int?, term: String): LiveData<List<Customer>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Customer>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.customer_GetByOrg(sm_id, org_Id, term) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getCustomers", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getCustomerById(cu_Id: Int): LiveData<Customer> {
        job = Job()
        return object : LiveData<Customer>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getCustomer_ById(cu_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call get by item", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun customer_getAgeDebit(cu_Id: Int): Customer? {
        try {
            val response =
                apiRequest { api.customer_getAgeDebit(cu_Id) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }

    override fun insertCustomer(baseEo: Customer): LiveData<Customer> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Customer>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.insertCustomer(baseEo) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR_CONNECTION)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call insertCustomer", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun customerSaveOrUpdate(baseEo: Customer): ResponseSingle<Customer> {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.insertCustomer(baseEo) }
            _networkState.postValue(NetworkState.LOADED)
            return response
        }catch (e: NoConnectivityException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            throw e
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR)
            throw e
        }
    }
    /// Function Name: Customer Payment Type Get All
    override fun getCptAll(term: String): LiveData<List<Customer_Payment_Type>> {
        job = Job()
        return object : LiveData<List<Customer_Payment_Type>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response =
                                apiRequest { api.getCPT_ById(term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProducts", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }
    // Function Name: Customer Group Get All
    override fun getCustomersGroups(term: String): LiveData<List<Customer_Group>> {
        job = Job()
        return object : LiveData<List<Customer_Group>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response =
                                apiRequest { api.getCustomerGroups(term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProducts", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun customersCategory_GetByTerm(term: String): LiveData<List<Customer_Category>> {
        job = Job()
        return object : LiveData<List<Customer_Category>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response =
                                apiRequest { api.customerCat_GetByTerm(term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call customersCategory_GetByTerm", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }
    // Function Name: Products Get for specific condition
    override fun getProducts(term: String,
                             warehouseId: Int?,
                             priceCode: String): LiveData<List<Product>> {
        job = Job()
        return object : LiveData<List<Product>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response =
                                apiRequest { api.products_GetByTerm(term, warehouseId, priceCode) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProducts", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }


    override suspend fun getProductForMarket(warehouseId: Int?, priceCode: String, currentDate: LocalDate, org_Id: Int?, cat_Id: Int?, br_Id: Int?, Term: String?, objCode: String, page: Int): List<Product>? {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response =  apiRequest { api.products_GetForMarket(warehouseId, priceCode, currentDate , org_Id, cat_Id, br_Id, Term, objCode, page, POST_PER_PAGE) }
            if (response.isSuccessful) {
                _networkState.postValue(NetworkState.LOADED)
                return response.data
            }
            _networkState.postValue(NetworkState.LOADED)
            return null
        } catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return null
        } catch (e: Exception){
            _networkState.postValue(NetworkState.ERROR)
            Log.e("Error Exception", "${e.message}", e)
            return null
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Error API Exception", "${e.message}", e)
            return null
        }
    }

    override suspend fun getProductForMarketSO(userId: Int?, priceCode: String, currentDate: LocalDate, org_Id: Int?, cat_Id: Int?, br_Id: Int?, Term: String?, objCode: String, page: Int): List<Product>? {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response =  apiRequest { api.products_GetForMarketSO(userId, priceCode, currentDate , org_Id, cat_Id, br_Id, Term, objCode, page, POST_PER_PAGE) }
            if (response.isSuccessful) {
                _networkState.postValue(NetworkState.LOADED)
                return response.data
            }
            _networkState.postValue(NetworkState.LOADED)
            return null
        } catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return null
        } catch (e: Exception){
            _networkState.postValue(NetworkState.ERROR)
            Log.e("Error Exception", "${e.message}", e)
            return null
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Error API Exception", "${e.message}", e)
            return null
        }
    }
    override suspend fun getProductForOffers(warehouseId: Int?, priceCode: String, currentDate: LocalDate, org_Id: Int?, Term: String?, objCode: String, page: Int): List<Product>? {
        try {
            val response =  apiRequest { api.products_GetForOffers(warehouseId, priceCode, currentDate , org_Id, Term, objCode, page, POST_PER_PAGE) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }

    override fun getProductsByPriceTerm(term: String, priceCode: String): LiveData<List<Product>> {
        job = Job()
        return object : LiveData<List<Product>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response =
                                apiRequest { api.products_GetByPriceTerm(term, priceCode) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProductsByPriceTerm", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getProductsByUserWarehouse(term: String, userId: Int?, priceCode: String): LiveData<List<Product>> {
        job = Job()
        return object : LiveData<List<Product>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response =
                                apiRequest { api.products_GetByUser(term, userId, priceCode) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProducts", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getProductsBySearch(term: String): LiveData<List<Product>> {
        job = Job()
        return object : LiveData<List<Product>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response =
                                apiRequest { api.products_GetBySearch(term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProductsBySearch", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getProductsByContract(contId: Int?, term: String): LiveData<List<Product>> {
        job = Job()
        return object : LiveData<List<Product>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response =
                                apiRequest { api.products_GetByContract(contId, term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProductsBySearch", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getProducts_InvoicesByCustomer(cu_Id: Int, prod_Id: Int, term: String ): LiveData<List<Product>> {
        job = Job()
        return object : LiveData<List<Product>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response =
                                apiRequest { api.product_GetInvoicessByCustomer(cu_Id, prod_Id, term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProductsByBatchs", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun productGetByBarcode(barcode: String,
                                     warehouseId: Int?,
                                     priceCode: String): LiveData<Product> {
        job = Job()
        return object : LiveData<Product>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest {
                                api.product_GetByBarcode(
                                    barcode,
                                    warehouseId,
                                    priceCode
                                )
                            }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call productGetByBarcode", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getProductById(prod_Id: Int): LiveData<Product> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProductPrice(prod_Id: Int): LiveData<Product_Price_List> {
        job = Job()
        return object : LiveData<Product_Price_List>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getProductPrice(prod_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProductById", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getServicesByTerm(term: String, priceCode: String): LiveData<List<Servs>> {
        job = Job()
        return object : LiveData<List<Servs>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getServicesByTerm(term, priceCode) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProductById", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun product_getLastPrice(prod_Id: Int, PriceCode: String, uomId: Int, curCode: String): Product_Price_List? {
        try {
            val response =
                apiRequest { api.product_getLastPrice(prod_Id, PriceCode, uomId, curCode) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }
    override fun getCurrency(cl_Id: Int): LiveData<Currency> {
        job = Job()
        return object : LiveData<Currency>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getCurrencyByClientId(cl_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getCurrency", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getCurrencyByCode(cr_symbol: String): LiveData<Currency> {
        job = Job()
        return object : LiveData<Currency>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getCurrencyByCode(cr_symbol) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getCurrencyByCode", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getRate(cr_Id: Int): LiveData<Currency_Rate> {
        job = Job()
        return object : LiveData<Currency_Rate>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getCurrencyLatestRate(cr_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getRate", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun brand_GetByTerm(term: String?): LiveData<List<Product_Brand>> {
        job = Job()
        return object : LiveData<List<Product_Brand>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getBrandByTerm(term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call brand get all", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun brandOnPages(term: String?, page: Int): List<Product_Brand>? {
        try {
            val response =
                apiRequest { api.brand_OnPages(term, page, POST_PER_PAGE) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }

    override suspend fun categoriesOnPages(term: String?, page: Int): List<Product_Category>? {
        try {
            val response =
                apiRequest { api.getCategoriesOnPages(term, page, POST_PER_PAGE) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }

    override fun categories_GetByTerm(term: String?): LiveData<List<Product_Category>> {
        job = Job()
        return object : LiveData<List<Product_Category>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getCategoriesByTerm(term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call Category get all", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getSalesman(pda_code: String): LiveData<Salesman> {
        job = Job()
        return object : LiveData<Salesman>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.salesmanGetByCode(pda_code) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getSalesman", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun salesman_GetAll(): LiveData<List<Salesman>> {
        job = Job()
        return object : LiveData<List<Salesman>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.salesmanGetAll() }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call salesman get all", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun salesman_getSummary(sm_Id: Int, selDate: String): LiveData<SalesmanSummary> {
        job = Job()
        return object : LiveData<SalesmanSummary>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getSalesmanSummary(sm_Id, selDate) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getSalesman", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun salesman_hasSalesPlan(sm_Id: Int): Salesman? {
        try {
            val response =
                apiRequest { api.salesmanHasSalesPlan(sm_Id) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }

    override suspend fun getDisc(userId: Int, discRange: String): UsersDiscounts? {
        try {
            val response =
                apiRequest { api.getUserDisc(userId, discRange) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }
    override suspend fun uom_GetByProduct(prod_Id: Int): List<UnitConvertion>? {
        try {
            val response =
                apiRequest { api.uom_GetByProduct(prod_Id) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }
    override fun getVoucherByCode(vo_code: String): LiveData<Voucher> {
        job = Job()
        return object : LiveData<Voucher>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getVoucherByCode(vo_code) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getVoucherByCode", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun priceCat_GetAll(): LiveData<List<PriceCategory>> {
        job = Job()
        return object : LiveData<List<PriceCategory>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getPriceCatAll() }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call price category get all", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun priceCat_GetBySalesman(sm_Id: Int): LiveData<List<PriceCategory>> {
        job = Job()
        return object : LiveData<List<PriceCategory>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getPriceCatBySalesman(sm_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call price category get by user", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun priceCat_GetById(prc_Id: Int): LiveData<PriceCategory> {
        job = Job()
        return object : LiveData<PriceCategory>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getPriceCategoryById(prc_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call price category get by id", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getDiscountItem(pr_Id: Int, currentDate: LocalDate, org_Id: Int?, price_cat_code: String): LiveData<Discount>  {
        job = Job()
        return object : LiveData<Discount>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.discount_GetCurrent(pr_Id, currentDate, org_Id, price_cat_code) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call get discount by product", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }
    override fun warehouse_GetBySalesman(sm_Id: Int): LiveData<List<Warehouse>> {
        job = Job()
        return object : LiveData<List<Warehouse>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.warehouse_GetSalesman(sm_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call warehouse for current salesman", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun warehouse_GetAll(): LiveData<List<Warehouse>> {
        job = Job()
        return object : LiveData<List<Warehouse>>(){
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.warehouse_GetAll() }
                            withContext(Main){
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call warehouse get all", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun location_GetByWhs(whsId: Int): LiveData<List<Loc>> {
        job = Job()
        return object : LiveData<List<Loc>>(){
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.location_GetByWhs(whsId) }
                            withContext(Main){
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call location get all", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun lookup_getByEntity(entity_name: String): LiveData<List<Lookups>> {
        job = Job()
        return object : LiveData<List<Lookups>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.loockup_getByEntity(entity_name) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call lookup for specific entity", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getSalesPaln(sm_Id: Int): LiveData<List<Lookups>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Lookups>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getSalesPlan(sm_Id) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getCustomers", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getRegions(): LiveData<List<Region>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Region>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getAllRegions() }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getCustomers", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    // For Maintenance
    override fun getMntStatus(): LiveData<List<MntStatus>> {
        job = Job()
        return object : LiveData<List<MntStatus>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getMntStatus() }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call Maintenance status for current salesman", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getMntType(): LiveData<List<MntType>> {
        job = Job()
        return object : LiveData<List<MntType>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getMntTyps() }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call Maintenance type for current salesman", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getWaiting_RegMnt(term: String): LiveData<List<RegMnt>> {
        job = Job()
        return object : LiveData<List<RegMnt>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getRegMnt(term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call Maintenance type for current salesman", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getInvoicsByTerm(cu_Id: Int?, term: String): LiveData<List<Sale>> {
        job = Job()
        return object : LiveData<List<Sale>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getInvoicesForMnt(cu_Id, term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call Maintenance type for current salesman", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getWarrantyByTerm(cu_Id: Int?, term: String): LiveData<List<Warranty>> {
        job = Job()
        return object : LiveData<List<Warranty>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getWarranty(cu_Id, term) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call Maintenance type for current salesman", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    // Get Document for stock-out
    override suspend fun saleItemsForStockout(sl_Id: Int, term: String,  whsId: Int, sotType: String, page: Int): List<InventoryDocLines>?{
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.InvDocLinessByDocEntry(sl_Id, term, whsId, sotType, page, POST_PER_PAGE) }
            if(response.isSuccessful){
                return response.data
            }
            return emptyList()
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return emptyList()
        }
        catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return emptyList()
        }catch (e: java.lang.Exception){
            _networkState.postValue(NetworkState.LOADING)
            Log.e("Connectivity", "Exception", e)
            return emptyList()
        }
    }

    override suspend fun invoices_SearchOnPages(term: String, whsId: Int, sotType: String, page: Int): List<InventoryDoc>? {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.InvDoc_SearchOnPages(term, whsId, sotType, page, POST_PER_PAGE) }
            if(response.isSuccessful){
                return response.data
            }
            return emptyList()
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("ApiError", "No internat connection", e)
            return emptyList()
        }
        catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return emptyList()
        }catch (e: java.lang.Exception){
            _networkState.postValue(NetworkState.LOADING)
            Log.e("Error", "Exception", e)
            return emptyList()
        }
    }

    // Get Document for stock-in
    override suspend fun doclinesForStockin(sl_Id: Int, term: String,  whsId: Int, sotType: String, page: Int): List<InventoryDocLines>?{
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.InvDocInLinessByDocEntry(sl_Id, term, whsId, sotType, page, POST_PER_PAGE) }
            if(response.isSuccessful){
                return response.data
            }
            return emptyList()
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return emptyList()
        }
        catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return emptyList()
        }catch (e: java.lang.Exception){
            _networkState.postValue(NetworkState.LOADING)
            Log.e("Connectivity", "Exception", e)
            return emptyList()
        }
    }

    override suspend fun docforStockin_SearchOnPages(term: String, whsId: Int, sotType: String, page: Int): List<InventoryDoc>? {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.InvDocIn_SearchOnPages(term, whsId, sotType, page, POST_PER_PAGE) }
            if(response.isSuccessful){
                return response.data
            }
            return emptyList()
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("ApiError", "No internat connection", e)
            return emptyList()
        }
        catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return emptyList()
        }catch (e: java.lang.Exception){
            _networkState.postValue(NetworkState.LOADING)
            Log.e("Error", "Exception", e)
            return emptyList()
        }
    }

    override fun cancelJob(){
        job?.cancel()
    }

    object loadUrlImage{
        @JvmStatic
        @BindingAdapter("android:urlRemoteImage")
        fun loadImage(view: ImageView, imgeUrl: String?) {

            val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.imagenotfound)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .dontAnimate()
                .dontTransform()

            Glide.with(view.context)
                .load(URL_GET_IMAGE + "/Products/" + (imgeUrl ?: "noimage")).apply(options)
                .into(view)
        }

        @JvmStatic
        @BindingAdapter("android:bitmapImage")
        fun loadImage(iv: ImageView, bmp: Bitmap?){
            if(bmp != null){
                iv.setImageBitmap(bmp)
            }
        }
    }
}