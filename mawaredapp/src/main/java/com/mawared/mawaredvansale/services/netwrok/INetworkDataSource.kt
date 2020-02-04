package com.mawared.mawaredvansale.services.netwrok

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.sales.*

interface INetworkDataSource {
    val downloadErrorMessage: LiveData<String>

    val downloadCustomers: LiveData<List<Customer>>
    suspend fun customersGetBySalesmanId(sm_Id: Int)

    val downloadCustomer: LiveData<Customer>
    suspend fun customerInsert(baseEo: Customer)

    val downloadCustomerGroup: LiveData<List<Customer_Group>>
    suspend fun customerGroupGetAll()

    val downloadCustomerPriceList: LiveData<List<Customer_Price_List>>
    suspend fun customerGetPriceList(sm_Id: Int)

    val downloadProducts: LiveData<List<Product>>
    suspend fun productGetByTerm(term: String, warehouseId: Int?, priceCode: String)

    val downloadProduct: LiveData<Product>
    suspend fun productGetByBarcode(barcode: String, warehouseId: Int?, priceCode: String)

    val downloadProductPriceList: LiveData<List<Product_Price_List>>
    suspend fun productPriceListGetAll()

    val downloadProductPrice: LiveData<Product_Price_List>
    suspend fun productPrice(prod_Id: Int)

    val downloadBrands: LiveData<List<Product_Brand>>
    suspend fun brandsGetAll()

    val downloadCategories: LiveData<List<Product_Category>>
    suspend fun categoryGetAll()

    val downloadCurrency: LiveData<Currency>
    suspend fun currencyGetByClientId(cl_Id: Int)

    val downloadCurrencyRate: LiveData<List<Currency_Rate>>
    suspend fun currencyRateGetAll()

    val downloadLatestCurrencyRate: LiveData<Currency_Rate>
    suspend fun currencyRateGetLatest(cr_Id: Int)

    val downloadCallCycle: LiveData<List<Call_Cycle>>
    suspend fun callCycleGetBySalesmanId(sm_Id: Int)

    val downloadRegion: LiveData<List<Region>>
    suspend fun regionsGetBySalesmanId(sm_Id: Int)

    val downloadSalesman: LiveData<Salesman>
    suspend fun salesmanGetVanSalesman(pda_code: String )

    // Invoices
    val downloadInvoices: LiveData<List<Sale>>
    suspend fun invoicesGetList(sm_Id: Int, customer_Id: Int?)

    val downloadInvoiceItems: LiveData<List<Sale_Items>>
    suspend fun invoiceItemsGetByInvoiceId(sl_Id: Int)

    val downloadInvoice: LiveData<Sale>
    suspend fun invoiceInsert(baseEo: Sale)
    // Sale Order
    val downloadOrders: LiveData<List<Sale_Order>>
    suspend fun ordersGetList(sm_Id: Int, customer_Id: Int?, vo_code: String)

    val downloadOrderItems: LiveData<List<Sale_Order_Items>>
    suspend fun orderItemsGetByOrderId(so_Id: Int)

    val downloadOrder : LiveData<Sale_Order>
    suspend fun orderInsert(baseEo: Sale_Order)

    // Voucher
    val downloadVoucher: LiveData<Voucher>
    suspend fun voucherGetByCode(vo_code: String)

}