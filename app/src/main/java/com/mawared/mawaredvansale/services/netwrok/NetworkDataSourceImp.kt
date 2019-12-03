package com.mawared.mawaredvansale.services.netwrok

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.sales.*
import com.mawared.mawaredvansale.utilities.ApiException
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class NetworkDataSourceImp(private val api: ApiService ): INetworkDataSource, SafeApiRequest() {

    // Error Message
    private val _downloadErrorMessage = MutableLiveData<String>()
    override val downloadErrorMessage: LiveData<String>
        get() = _downloadErrorMessage

    // Product
    private val _downloadProducts = MutableLiveData<List<Product>>()
    override val downloadProducts: LiveData<List<Product>>
        get() = _downloadProducts

    override suspend fun productGetByTerm(term: String, warehouseId: Int?, priceCode: String) {
        try {
            val response = apiRequest { api.products_GetByTerm(term, warehouseId, priceCode) }
            _downloadProducts.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    private val _downloadProduct = MutableLiveData<Product>()
    override val downloadProduct: LiveData<Product>
        get() = _downloadProduct

    override suspend fun productGetByBarcode(
        barcode: String, warehouseId: Int?, priceCode: String
    ) {
        try {
            val response = apiRequest { api.product_GetByBarcode(barcode, warehouseId, priceCode) }
            _downloadProduct.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // Brand
    private val _downloadBrands = MutableLiveData<List<Product_Brand>>()
    override val downloadBrands: LiveData<List<Product_Brand>>
        get() = _downloadBrands

    override suspend fun brandsGetAll() {
        try {
            val response = apiRequest { api.getAllBrand() }
            _downloadBrands.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // Category
    private val _downloadCategories = MutableLiveData<List<Product_Category>>()
    override val downloadCategories: LiveData<List<Product_Category>>
        get() = _downloadCategories

    override suspend fun categoryGetAll() {
        try {
            val response = apiRequest { api.getAllCategories() }
            _downloadCategories.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // Product price list
    private val _downloadProductPriceList = MutableLiveData<List<Product_Price_List>>()
    override val downloadProductPriceList: LiveData<List<Product_Price_List>>
        get() = _downloadProductPriceList

    override suspend fun productPriceListGetAll() {
        try {
            val response = apiRequest { api.getProductPriceList() }
            _downloadProductPriceList.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    private val _downloadProductPrice = MutableLiveData<Product_Price_List>()
    override val downloadProductPrice: LiveData<Product_Price_List>
        get() = _downloadProductPrice

    override suspend fun productPrice(prod_Id: Int) {
        try {
            val response = apiRequest { api.getProductPrice(prod_Id) }
            _downloadProductPrice.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // Customer
    private val _downloadCustomers = MutableLiveData<List<Customer>>()
    override val downloadCustomers: LiveData<List<Customer>>
        get() = _downloadCustomers

    override suspend fun customersGetBySalesmanId(sm_Id: Int) {
        try {
            val response = apiRequest { api.getAllCustomers(sm_Id) }
            _downloadCustomers.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    private val _downloadCustomer = MutableLiveData<Customer>()
    override val downloadCustomer: LiveData<Customer>
        get() = _downloadCustomer

    override suspend fun customerInsert(baseEo: Customer) {
        try {
            val response = apiRequest { api.insertCustomer(baseEo) }
            _downloadCustomer.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }


    // Customer Group
    private val _downloadCustomerGroup = MutableLiveData<List<Customer_Group>>()
    override val downloadCustomerGroup: LiveData<List<Customer_Group>>
        get() = _downloadCustomerGroup

    override suspend fun customerGroupGetAll() {

    }

    // Customer Price list
    private val _downloadCustomerPriceList = MutableLiveData<List<Customer_Price_List>>()
    override val downloadCustomerPriceList: LiveData<List<Customer_Price_List>>
        get() = _downloadCustomerPriceList

    override suspend fun customerGetPriceList(sm_Id: Int) {

    }

    // Currencies
    private val _downloadCurrency = MutableLiveData<Currency>()
    override val downloadCurrency: LiveData<Currency>
        get() = _downloadCurrency

    override suspend fun currencyGetByClientId(cl_Id: Int) {
        try {
            val response = apiRequest { api.getCurrencyByClientId(cl_Id) }
            _downloadCurrency.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // List of Currency rate
    private val _downloadCurrencyRate = MutableLiveData<List<Currency_Rate>>()
    override val downloadCurrencyRate: LiveData<List<Currency_Rate>>
        get() = _downloadCurrencyRate

    override suspend fun currencyRateGetAll() {
        try {
            val response = apiRequest { api.getCurrencyRate() }
            _downloadCurrencyRate.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // Currency rate
    private val _downloadLatestCurrencyRate = MutableLiveData<Currency_Rate>()
    override val downloadLatestCurrencyRate: LiveData<Currency_Rate>
        get() = _downloadLatestCurrencyRate

    override suspend fun currencyRateGetLatest(cr_Id: Int) {
        try {
            val response = apiRequest { api.getCurrencyLatestRate(cr_Id) }
            _downloadLatestCurrencyRate.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // Call Cycle
    private val _downloadCallCycle = MutableLiveData<List<Call_Cycle>>()
    override val downloadCallCycle: LiveData<List<Call_Cycle>>
        get() = _downloadCallCycle

    override suspend fun callCycleGetBySalesmanId(sm_Id: Int) {

    }

    // Region
    private val _downloadRegion = MutableLiveData<List<Region>>()
    override val downloadRegion: LiveData<List<Region>>
        get() = _downloadRegion

    override suspend fun regionsGetBySalesmanId(sm_Id: Int) {
        try {
            val response = apiRequest { api.getAllRegions() }
            _downloadRegion.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // Salesman
    private val _downloadSalesman = MutableLiveData<Salesman>()
    override val downloadSalesman: LiveData<Salesman>
        get() = _downloadSalesman

    override suspend fun salesmanGetVanSalesman(pda_code: String) {
        try {
            val response = apiRequest { api.salesmanGetByCode(pda_code) }
            _downloadSalesman.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // Invoices
    private val _downloadInvoices = MutableLiveData<List<Sale>>()
    override val downloadInvoices: LiveData<List<Sale>>
        get() = _downloadInvoices

    override suspend fun invoicesGetList(sm_Id: Int, customer_Id: Int?) {
        try {
            val response = apiRequest { api.getInvoices(sm_Id, customer_Id) }
            _downloadInvoices.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    private val _downloadInvoiceItems = MutableLiveData<List<Sale_Items>>()
    override val downloadInvoiceItems: LiveData<List<Sale_Items>>
        get() = _downloadInvoiceItems

    override suspend fun invoiceItemsGetByInvoiceId(sl_Id: Int) {
        try {
            val response = apiRequest { api.getInvoiceItemsByInvoiceId(sl_Id) }
            _downloadInvoiceItems.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    private val _downloadInvoice = MutableLiveData<Sale>()
    override val downloadInvoice: LiveData<Sale>
        get() = _downloadInvoice

    override suspend fun invoiceInsert(baseEo: Sale) {
        try {
            val response = apiRequest { api.insertInvoice(baseEo) }
            _downloadInvoice.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    // Sale Order
    private val _downloadOrders = MutableLiveData<List<Sale_Order>>()
    override val downloadOrders: LiveData<List<Sale_Order>>
        get() = _downloadOrders

    override suspend fun ordersGetList(sm_Id: Int, customer_Id: Int?) {
        try {
            val response = apiRequest { api.getOrders(sm_Id, customer_Id) }
            _downloadOrders.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    private val _downloadOrderItems = MutableLiveData<List<Sale_Order_Items>>()
    override val downloadOrderItems: LiveData<List<Sale_Order_Items>>
        get() = _downloadOrderItems

    override suspend fun orderItemsGetByOrderId(so_Id: Int) {
        try {
            val response = apiRequest { api.getOrderItemsByOrderId(so_Id) }
            _downloadOrderItems.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    private val _downloadOrder = MutableLiveData<Sale_Order>()
    override val downloadOrder: LiveData<Sale_Order>
        get() = _downloadOrder

    override suspend fun orderInsert(baseEo: Sale_Order) {
        try {
            val response = apiRequest { api.insertOrder(baseEo) }
            _downloadOrder.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }

    private val _downloadVoucher = MutableLiveData<Voucher>()
    override val downloadVoucher: LiveData<Voucher>
        get() = _downloadVoucher

    override suspend fun voucherGetByCode(vo_code: String) {
        try {
            val response = apiRequest { api.getVoucherByCode(vo_code) }
            _downloadVoucher.postValue(response.data)

        } catch (e: ApiException) {
            Log.e("Connectivity", "No internat connection", e)
            _downloadErrorMessage.postValue("No internat connection")
        }
    }
}