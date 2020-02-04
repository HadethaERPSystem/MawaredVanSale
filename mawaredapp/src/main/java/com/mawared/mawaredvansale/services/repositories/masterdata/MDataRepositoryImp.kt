package com.mawared.mawaredvansale.services.repositories.masterdata

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.md.Currency
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.SingleRecResponse
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.URL_IMAGE
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

private val MINIMUM_INTERVAL = 6

class MDataRepositoryImp(private val api: ApiService, private val db: AppDatabase): IMDataRepository, SafeApiRequest() {

    var job: CompletableJob? = null
    private val customers = MutableLiveData<List<Customer>>()

    init {
        customers.observeForever {
            saveCustomers(it)
        }
    }

    override suspend fun getCustomer(): LiveData<List<Customer>>{
        return withContext(IO){
            fetchCustomers()
            db.getCustomerDao().getAll()
        }
    }

    private fun saveCustomers(customers: List<Customer>){
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val stDate = now.toString()
        App.prefs.savedAt = stDate

        Coroutines.io{
            db.getCustomerDao().deleteAll()
            db.getCustomerDao().insert(customers)
        }
    }

    private suspend fun fetchCustomers(){
        val lastSavedAt = App.prefs.savedAt
        val sm_Id = App.prefs.savedSalesman!!.sm_user_id!!
        if(lastSavedAt == null || isFetchNeeded(lastSavedAt)){
            val response = apiRequest { api.customer_GetByOrg(sm_Id, App.prefs.saveUser?.org_Id) }
            customers.postValue(response.data)
        }
    }

    private fun isFetchNeeded(savedAt: String): Boolean{
        val dt = LocalDateTime.parse(savedAt, DateTimeFormatter.ISO_DATE_TIME)

        return ChronoUnit.HOURS.between(dt, LocalDateTime.now(ZoneOffset.UTC)) > MINIMUM_INTERVAL
    }

    override fun getCustomers(sm_Id: Int): LiveData<List<Customer>> {
        job = Job()
        return object : LiveData<List<Customer>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getAllCustomers(sm_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getCustomers", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun customers_getSchedule(sm_Id: Int): LiveData<List<Customer>> {
        job = Job()
        return object : LiveData<List<Customer>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.customers_getSchedule(sm_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call customers schedule", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }
    override fun getCustomersByOrg(sm_Id: Int, org_Id: Int?): LiveData<List<Customer>> {
        job = Job()
        return object : LiveData<List<Customer>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.customer_GetByOrg(sm_Id, org_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
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

    override fun insertCustomer(baseEo: Customer): LiveData<Customer> {
        job = Job()
        return object : LiveData<Customer>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {

                            val response = apiRequest { api.insertCustomer(baseEo) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call insertCustomer", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun customerSaveOrUpdate(baseEo: Customer): SingleRecResponse<Customer> {
        try {
            val response = apiRequest { api.insertCustomer(baseEo) }
            return response
        }catch (e: NoConnectivityException){
            throw e
        }catch (e: ApiException){
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

    override fun product_getLastPrice(prod_Id: Int, PriceCode: String): LiveData<Product_Price_List> {
        job = Job()
        return object : LiveData<Product_Price_List>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.product_getLastPrice(prod_Id, PriceCode) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call get last price", e)
                            return@launch
                        }
                    }
                }
            }
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

    override fun getDiscountItem(pr_Id: Int, currentDate: LocalDate, org_Id: Int?): LiveData<Discount>  {
        job = Job()
        return object : LiveData<Discount>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.discount_GetCurrent(pr_Id, currentDate, org_Id) }
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

    override fun cancelJob(){
        job?.cancel()
    }

    object loadUrlImage{
        @JvmStatic
        @BindingAdapter("android:urlRemoteImage")
        fun loadImage(view: ImageView, imgeUrl: String){
            Glide.with(view.context)
                .load(URL_IMAGE + imgeUrl).apply(RequestOptions().fitCenter())
                //.error(R.drawable.ic_broken_image) //6
                //.fallback(R.drawable.ic_no_image) //7
                .into(view)
        }
    }
}