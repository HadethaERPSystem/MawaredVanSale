package com.mawared.mawaredvansale.services.netwrok

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout_Items
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.md.Currency
import com.mawared.mawaredvansale.data.db.entities.sales.*
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.data.db.entities.security.User
import com.mawared.mawaredvansale.services.netwrok.responses.AuthResponse
import com.mawared.mawaredvansale.services.netwrok.responses.ListRecsResponse
import com.mawared.mawaredvansale.services.netwrok.responses.SingleRecResponse
import com.mawared.mawaredvansale.utilities.*
import okhttp3.OkHttpClient
import org.threeten.bp.LocalDate
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*

interface ApiService {
    @FormUrlEncoded
    @POST(URL_LOGIN)
    suspend fun userLogin(
        @Field("user_name") user_name: String,
        @Field("password") password: String
    ) : Response<AuthResponse>

    @POST(URL_LOGIN)
    suspend fun login(@Body user: User) : Response<AuthResponse>

    @GET(URL_USER_MENU)
    suspend fun menu_getByUser(@Query("UserId") UserId: Int, @Query("lang") lang: String): Response<ListRecsResponse<Menu>>

    @GET(URL_ALL_PRODUCTS)
    suspend fun products_GetByTerm(@Query("Term") term: String,
                                   @Query("WarehouseId") warehouseId: Int?,
                                   @Query("PriceCode") priceCode: String) : Response<ListRecsResponse<Product>>
    @GET(URL_ALL_PRODUCTS_PRICE_CAT)
    suspend fun products_GetByPriceTerm(@Query("Term") term: String,
                                   @Query("PriceCode") priceCode: String) : Response<ListRecsResponse<Product>>
    //products_GetByUser
    @GET(URL_PRODUCTS_BY_USER)
    suspend fun products_GetByUser(@Query("Term") term: String,
                                   @Query("UserId") userId: Int?,
                                   @Query("PriceCode") priceCode: String) : Response<ListRecsResponse<Product>>
    @GET(URL_PRODUCTS_BY_SEARCH)
    suspend fun products_GetBySearch(@Query("Term") term: String) : Response<ListRecsResponse<Product>>
    @GET(URL_ALL_PRODUCTS)
    suspend fun product_GetByBarcode(@Query("Barcode") barcode: String,
                               @Query("WarehouseId") warehouseId: Int?,
                               @Query("PriceCode") priceCode: String) : Response<SingleRecResponse<Product>>

    // for master data
    @GET(URL_ALL_PRODUCTS_PRICE_LIST)
    suspend fun getProductPriceList(): Response<ListRecsResponse<Product_Price_List>>

    @GET(URL_PRODUCT_PRICE)
    suspend fun getProductPrice(@Query("prod_Id") prod_Id: Int): Response<SingleRecResponse<Product_Price_List>>
    @GET(URL_PRODUCT_LAST_PRICE)
    suspend fun product_getLastPrice(@Query("prod_id") prod_Id: Int, @Query("PriceCode") priceCode: String): Response<SingleRecResponse<Product_Price_List>>

    @GET(URL_ALL_CUSTOMERS)
    suspend fun getAllCustomers(@Query("sm_Id") sm_Id: Int): Response<ListRecsResponse<Customer>>
    @GET(URL_SCHEDULE_CUSTOMERS)
    suspend fun customers_getSchedule(@Query("sm_Id") sm_Id: Int): Response<ListRecsResponse<Customer>>
    @GET(URL_CUSTOMERS_BY_ORG)
    suspend fun customer_GetByOrg(@Query("sm_Id") sm_Id: Int, @Query("org_Id") org_Id: Int?): Response<ListRecsResponse<Customer>>
    @GET(URL_CUSTOMER_BY_Id)
    suspend fun getCustomer_ById(@Query("cu_Id") cu_Id: Int): Response<SingleRecResponse<Customer>>

    @POST(URL_INSERT_CUSTOMER)
    suspend fun insertCustomer(@Body baseEo: Customer): Response<SingleRecResponse<Customer>>

    //////// Customer Group
    @GET(URL_ALL_CUSTOMER_GROUP)
    suspend fun getCustomerGroups(@Query("term") term: String): Response<ListRecsResponse<Customer_Group>>
    @GET(URL_ALL_CUSTOMER_CATEGORY)
    suspend fun customerCat_GetByTerm(@Query("term") term: String): Response<ListRecsResponse<Customer_Category>>
    ////////CUSTOMER PAYMENT TYPE
    @GET(URL_ALL_CPT)
    suspend fun getCPT_ById(@Query("term") term: String): Response<ListRecsResponse<Customer_Payment_Type>>

    //////////////////////////////
    @GET(URL_ALL_CURRENCIES)
    suspend fun getAllCurrencies(): Response<ListRecsResponse<Currency>>

    @GET(URL_CURRENCY_BY_CLIENT_ID)
    suspend fun getCurrencyByClientId(@Query("cl_Id") cl_Id: Int): Response<SingleRecResponse<Currency>>

    @GET(URL_CURRENCY_BY_CODE)
    suspend fun getCurrencyByCode(@Query("cr_symbol") cr_symbol: String): Response<SingleRecResponse<Currency>>
    @GET(URL_ALL_CURRENCIES_RATE)
    suspend fun getCurrencyRate(): Response<ListRecsResponse<Currency_Rate>>

    @GET(URL_CURRENT_CURRENCY_RATE)
    suspend fun getCurrencyLatestRate(@Query("cr_Id") cr_Id: Int): Response<SingleRecResponse<Currency_Rate>>

    @GET(URL_ALL_PRODUCTS_BRAND)
    suspend fun getAllBrand(): Response<ListRecsResponse<Product_Brand>>

    @GET(URL_ALL_PRODUCTS_CATEGORY)
    suspend fun getAllCategories(): Response<ListRecsResponse<Product_Category>>

    @GET(URL_ALL_REGION)
    suspend fun getAllRegions(): Response<ListRecsResponse<Region>>

    @GET(URL_ALL_SALESMAN_BY_CODE)
    suspend fun salesmanGetByCode(@Query("pda_code") pda_code: String): Response<SingleRecResponse<Salesman>>
    @GET(URL_GET_SALESMAN_BY_USER)
    suspend fun salesmanGetByUser(@Query("UserId") userId: Int): Response<SingleRecResponse<Salesman>>
    @GET(URL_ALL_SALESMAN)
    suspend fun salesmanGetAll(): Response<ListRecsResponse<Salesman>>

    @GET(URL_ALL_SALESMAN_CUSTOMERS)
    suspend fun getAllSalesmanCustomers(): Response<ListRecsResponse<Salesman_Customer>>

    // Voucher
    @GET(URL_VOUCHER_BY_CODE)
    suspend fun getVoucherByCode(@Query("vo_code") vo_code: String): Response<SingleRecResponse<Voucher>>

    // Price category
    @GET(URL_PRICE_CAT_BY_All)
    suspend fun getPriceCatAll(): Response<ListRecsResponse<PriceCategory>>
    @GET(URL_PRICE_CAT_BY_ID)
    suspend fun getPriceCategoryById(@Query("prc_Id") prc_Id: Int): Response<SingleRecResponse<PriceCategory>>

    // Discount
    @GET(URL_DISCOUNT_BY_PRODUCT)
    suspend fun discount_GetCurrent(@Query("pr_Id") pr_Id: Int, @Query("CurrentDate") currentDate: LocalDate, @Query("org_Id") org_Id: Int?): Response<SingleRecResponse<Discount>>

    @GET(URL_WAREHOUSE_GET_ALL)
    suspend fun warehouse_GetAll() : Response<ListRecsResponse<Warehouse>>

    @GET(URL_WAREHOUSE_GET_BY_SALESMAN)
    suspend fun warehouse_GetSalesman(@Query("sm_Id") sm_Id: Int) : Response<ListRecsResponse<Warehouse>>
    // for delivery
    @GET(URL_ALL_DELIVERY_BY_SALESMANID)
    suspend fun getDelivery_BySalesmandId(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?): Response<ListRecsResponse<Delivery>>

    @GET(URL_DELIVERY_BY_ID)
    suspend fun getDeliveryById(@Query("dl_Id") dl_Id: Int?): Response<SingleRecResponse<Delivery>>

    @POST(URL_DELIVERY_UPDATE)
    suspend fun updateDelivery(@Body baseEo: Delivery): Response<SingleRecResponse<Delivery>>

    @GET(URL_ALL_DELIVERY_DETAILS)
    suspend fun getAllDeliveryDetails(@Query("dl_Id") dl_Id: Int): Response<ListRecsResponse<Delivery_Items>>

    // for Check load
    @GET(URL_ALL_CHECK_LOAD)
    suspend fun getAllCheckLoad(@Query("sm_Id") sm_Id: Int): Response<ListRecsResponse<Check_Load>>

    @GET(URL_ALL_CHECK_LOAD_DETAILS)
    suspend fun getAllCheckLoadDetails(@Query("sm_Id") sm_Id: Int): Response<ListRecsResponse<Check_Load_Details>>

    // Manage Sale
    @POST(URL_ADD_SALE)
    suspend fun insertInvoice(@Body baseEo: Sale) : Response<SingleRecResponse<Sale>>

    @GET(URL_SALE_BY_ID)
    suspend fun getInvoiceById(@Query("sl_Id") sl_Id: Int) : Response<SingleRecResponse<Sale>>

    @GET(URL_SALES_FOR_SALESMAN)
    suspend fun getInvoices(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?) : Response<ListRecsResponse<Sale>>

    @GET(URL_SALES_ITEMS)
    suspend fun getInvoiceItemsByInvoiceId(@Query("sl_Id") sl_Id: Int) : Response<ListRecsResponse<Sale_Items>>

    @GET(URL_SALES_DELETE)
    suspend fun deleteInvoice(@Query("sl_Id") sl_Id: Int) : Response<SingleRecResponse<String>>
    // Sale Return
    //////////////////////////////////////////////////////////////////////////////////////////
    @POST(URL_ADD_SALE_RETURN)
    suspend fun insertReturn(@Body baseEo: Sale_Return) : Response<SingleRecResponse<Sale_Return>>

    @GET(URL_SALE_RETURN_BY_ID)
    suspend fun getSaleReturnById(@Query("sr_Id") sr_Id: Int) : Response<SingleRecResponse<Sale_Return>>

    @GET(URL_SALE_RETURN_FOR_SALESMAN)
    suspend fun getReturn(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?) : Response<ListRecsResponse<Sale_Return>>

    @GET(URL_SALE_RETURN_DELETE)
    suspend fun deleteReturn(@Query("sr_Id") sr_Id: Int) : Response<SingleRecResponse<String>>

    @GET(URL_SALE_RETURN_ITEMS)
    suspend fun getReturnItemsByReturnId(@Query("sr_Id") sr_Id: Int) : Response<ListRecsResponse<Sale_Return_Items>>

    // Receivable
    //////////////////////////////////////////////////////////////////////////////////////////
    @POST(URL_ADD_RECEIVABLE)
    suspend fun insertReceivable(@Body baseEo: Receivable) : Response<SingleRecResponse<Receivable>>
    @GET(URL_RECEIVABLE_BY_ID)
    suspend fun getReceivableById(@Query("py_Id") py_Id: Int) : Response<SingleRecResponse<Receivable>>
    @GET(URL_RECEIVABLE_FOR_CUSTOMERS)
    suspend fun getReceivable(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?) : Response<ListRecsResponse<Receivable>>
    @GET(URL_RECEIVABLE_DELETE)
    suspend fun deleteReceivable(@Query("rcv_Id") rcv_Id: Int) : Response<SingleRecResponse<String>>

    // Payable
    //////////////////////////////////////////////////////////////////////////////////////////
    @POST(URL_ADD_PAYABLE)
    suspend fun insertPayable(@Body baseEo: Payable) : Response<SingleRecResponse<Payable>>
    @GET(URL_PAYABLE_BY_ID)
    suspend fun getPayableById(@Query("py_Id") py_Id: Int) : Response<SingleRecResponse<Payable>>
    @GET(URL_PAYABLE_FOR_CUSTOMERS)
    suspend fun getPayable(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?) : Response<ListRecsResponse<Payable>>
    @GET(URL_PAYABLE_DELETE)
    suspend fun deletePayable(@Query("py_Id") py_Id: Int) : Response<SingleRecResponse<String>>
    //////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////////////
    // Mange Sale Order
    @POST(URL_ADD_ORDER)
    suspend fun insertOrder(@Body order: Sale_Order) : Response<SingleRecResponse<Sale_Order>>

    @POST(URL_UPDATE_ORDER)
    suspend fun updateOder(@Body order: Sale_Order) : Response<SingleRecResponse<Sale_Order>>

    @GET(URL_ORDER_BY_ID)
    suspend fun getOrderById(@Query("so_Id") so_Id: Int) : Response<SingleRecResponse<Sale_Order>>

    @GET(URL_ORDER_BY_CUSTOMERS)
    suspend fun getOrderByCustomerId(@Query("cu_Id") cu_Id: Int) : Response<ListRecsResponse<Sale_Order>>

    @GET(URL_ORDER_BY_SALESMAN)
    suspend fun getOrders(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?, @Query("vo_code") vo_code: String) : Response<ListRecsResponse<Sale_Order>>

    @GET(URL_ORDER_DELETE)
    suspend fun deleteOrder(@Query("so_Id") so_Id: Int) : Response<SingleRecResponse<String>>

    @GET(URL_ORDER_ITEMS)
    suspend fun getOrderItemsByOrderId(@Query("so_Id") so_Id: Int) : Response<ListRecsResponse<Sale_Order_Items>>

    // Inventory Stock-in
    @POST(URL_ADD_STOCK_IN)
    suspend fun insertStockIn(@Body baseEo: Stockin) : Response<SingleRecResponse<Stockin>>

    @POST(URL_UPDATE_STOCK_IN)
    suspend fun updateStockIn(@Body baseEo: Stockin) : Response<SingleRecResponse<Stockin>>

    @GET(URL_STOCK_IN_BY_ID)
    suspend fun getStockInById(@Query("sin_Id") sin_Id: Int) : Response<SingleRecResponse<Stockin>>

    @GET(URL_STOCK_IN_BY_CUSTOMERS)
    suspend fun getStockInByUserId(@Query("user_Id") user_Id: Int) : Response<ListRecsResponse<Stockin>>

    @GET(URL_STOCK_IN_ITEMS)
    suspend fun getStockInItemsById(@Query("sin_Id") sin_Id: Int) : Response<ListRecsResponse<Stockin_Items>>


    // Inventory Stock-out
    @POST(URL_ADD_STOCK_OUT)
    suspend fun insertStockOut(@Body baseEo: Stockout) : Response<SingleRecResponse<Stockout>>

    @POST(URL_UPDATE_STOCK_OUT)
    suspend fun updateStockOut(@Body baseEo: Stockout) : Response<SingleRecResponse<Stockout>>

    @GET(URL_STOCK_IN_BY_ID)
    suspend fun getStockOutById(@Query("sot_Id") sot_Id: Int) : Response<SingleRecResponse<Stockout>>

    @GET(URL_STOCK_OUT_BY_CUSTOMERS)
    suspend fun getStockOutByUserId(@Query("user_Id") user_Id: Int) : Response<ListRecsResponse<Stockout>>

    @GET(URL_STOCK_OUT_ITEMS)
    suspend fun getStockOutItemsById(@Query("sot_Id") sot_Id: Int) : Response<ListRecsResponse<Stockout_Items>>

    @POST(URL_ADD_TRANSFER)
    suspend fun transfer_SaveOrUpdate(@Body baseEo: Transfer): Response<SingleRecResponse<Transfer>>
    @GET(URL_TRANSFER_GET_BY_ID)
    suspend fun transfer_getById(@Query("tr_Id") tr_Id: Int): Response<SingleRecResponse<Transfer>>
    @GET(URL_TRANSFER_GET_BY_User)
    suspend fun transfer_getByUserId(@Query("userId") userId: Int): Response<ListRecsResponse<Transfer>>
    @GET(URL_TRANSFER_GET_BY_MASTER_ID)
    suspend fun transfer_GetItemsByMasterId(@Query("tr_Id") tr_Id: Int) : Response<ListRecsResponse<Transfer_Items>>

    companion object{
        operator fun invoke(connectivityInterceptor: ConnectivityInterceptor) : ApiService{
            // define okHttpclient for checking if connection is available or not
            val okHttpclient = OkHttpClient.Builder()
                .addInterceptor(connectivityInterceptor)
                .build()


            return Retrofit.Builder()
                .client(okHttpclient) // for checking if connection is avialable or not
                .baseUrl(BASE_URL_API)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}