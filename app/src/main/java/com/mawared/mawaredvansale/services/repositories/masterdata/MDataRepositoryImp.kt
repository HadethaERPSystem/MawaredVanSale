package com.mawared.mawaredvansale.services.repositories.masterdata

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.URL_IMAGE
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MDataRepositoryImp(private val api: ApiService): IMDataRepository, SafeApiRequest() {

    var job: CompletableJob? = null

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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getCustomerByTerm", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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
                            Log.e("Connectivity", "No internat connection", e)
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