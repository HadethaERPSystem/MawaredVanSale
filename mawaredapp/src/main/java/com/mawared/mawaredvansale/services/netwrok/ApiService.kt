package com.mawared.mawaredvansale.services.netwrok

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mawared.mawaredvansale.data.db.entities.reports.fms.CashbookStatement
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.data.db.entities.inventory.*
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.md.Currency
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatement
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatus
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash1
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash2
import com.mawared.mawaredvansale.data.db.entities.reports.sales.SalesStatement
import com.mawared.mawaredvansale.data.db.entities.reports.stock.StockStatement
import com.mawared.mawaredvansale.data.db.entities.sales.*
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.data.db.entities.security.User
import com.mawared.mawaredvansale.services.netwrok.responses.AuthResponse
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseList
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.utilities.*
import okhttp3.*
import org.threeten.bp.LocalDate
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*
import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import java.lang.Exception
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*


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
    suspend fun menu_getByUser(@Query("UserId") UserId: Int, @Query("lang") lang: String): Response<ResponseList<Menu>>

    //////////////////////////////////////////////////////////////////////////////////////////
    //// PRODUCTS
    //////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_ALL_PRODUCTS)
    suspend fun products_GetByTerm(@Query("Term") term: String,
                                   @Query("WarehouseId") warehouseId: Int?,
                                   @Query("PriceCode") priceCode: String) : Response<ResponseList<Product>>

    @GET(URL_PRODUCTS_GET_FOR_MARKET_PLACE)
    suspend fun products_GetForMarket(@Query("WarehouseId") warehouseId: Int?,
                                      @Query("PriceCode") priceCode: String,
                                      @Query("currentDate") currentDate: LocalDate,
                                      @Query("org_Id") org_Id: Int?,
                                      @Query("cat_Id") cat_Id: Int?,
                                      @Query("brand_Id") br_Id: Int?,
                                      @Query("Term") Term: String?,
                                      @Query("objCode") objCode: String,
                                      @Query("page") page: Int,
                                      @Query("pageSize") pageSize: Int) : Response<ResponseList<Product>>

    @GET(URL_PRODUCTS_GET_FOR_MARKET_PLACE_SO)
    suspend fun products_GetForMarketSO(@Query("userId") userId: Int?,
                                      @Query("PriceCode") priceCode: String,
                                      @Query("currentDate") currentDate: LocalDate,
                                      @Query("org_Id") org_Id: Int?,
                                      @Query("cat_Id") cat_Id: Int?,
                                      @Query("brand_Id") br_Id: Int?,
                                      @Query("Term") Term: String?,
                                      @Query("objCode") objCode: String,
                                      @Query("page") page: Int,
                                      @Query("pageSize") pageSize: Int) : Response<ResponseList<Product>>

    @GET(URL_PRODUCTS_GET_FOR_OFFERS)
    suspend fun products_GetForOffers(@Query("WarehouseId") warehouseId: Int?,
                                      @Query("PriceCode") priceCode: String,
                                      @Query("currentDate") currentDate: LocalDate,
                                      @Query("org_Id") org_Id: Int?,
                                      @Query("Term") Term: String?,
                                      @Query("objCode") objCode: String,
                                      @Query("page") page: Int,
                                      @Query("pageSize") pageSize: Int) : Response<ResponseList<Product>>

    @GET(URL_PRODUCTS_GET_WAREHOUSE_ON_PAGES)
    suspend fun products_GetByWareOnPages(@Query("Term") term: String,
                                   @Query("WarehouseId") warehouseId: Int?,
                                   @Query("PriceCode") priceCode: String,
                                   @Query("page") page: Int,
                                   @Query("pageSize") pageSize: Int) : Response<ResponseList<Product>>

    @GET(URL_ALL_PRODUCTS_PRICE_CAT)
    suspend fun products_GetByPriceTerm(@Query("Term") term: String,
                                   @Query("PriceCode") priceCode: String) : Response<ResponseList<Product>>
    @GET(URL_PRODUCTS_GET_ON_PAGES)
    suspend fun products_GetOnPages(@Query("Term") term: String,
                                    @Query("PriceCode") priceCode: String,
                                    @Query("page") page: Int,
                                    @Query("pageSize") pageSize: Int) : Response<ResponseList<Product>>
    //products_GetByUser
    @GET(URL_PRODUCTS_BY_USER)
    suspend fun products_GetByUser(@Query("Term") term: String,
                                   @Query("UserId") userId: Int?,
                                   @Query("PriceCode") priceCode: String) : Response<ResponseList<Product>>

    @GET(URL_PRODUCTS_GET_USER_ON_PAGES)
    suspend fun products_GetByUserOnPages(@Query("Term") term: String,
                                   @Query("UserId") userId: Int?,
                                   @Query("PriceCode") priceCode: String,
                                   @Query("page") page: Int,
                                   @Query("pageSize") pageSize: Int) : Response<ResponseList<Product>>

    @GET(URL_PRODUCTS_BY_SEARCH)
    suspend fun products_GetBySearch(@Query("Term") term: String) : Response<ResponseList<Product>>

    @GET(URL_PRODUCTS_BY_SEARCH_DOC)
    suspend fun products_GetBySearchDoc(@Query("Term") term: String, @Query("docId") docId: Int) : Response<ResponseList<Product>>

    @GET(URL_PRODUCTS_GET_BY_CONTRACT)
    suspend fun products_GetByContract(@Query("contId") contId: Int?, @Query("term") term: String): Response<ResponseList<Product>>

    @GET(URL_PRODUCT_BY_BARCODE)
    suspend fun product_GetByBarcode(@Query("Barcode") barcode: String,
                               @Query("WarehouseId") warehouseId: Int?,
                               @Query("PriceCode") priceCode: String) : Response<ResponseSingle<Product>>
    @GET(URL_INVOICES_BY_CUSTOMER)
    suspend fun product_GetInvoicessByCustomer(@Query("cu_Id") cu_Id: Int,
                                                 @Query("term") term: String): Response<ResponseList<DocRefDto>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_SERVICES)
    suspend fun getServicesByTerm(@Query("term") term: String, @Query("priceCode") priceCode: String): Response<ResponseList<Servs>>

    //////////////////////////////////////////////////////////////////////////////////////////
    //// PRICES
    //////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_ALL_PRODUCTS_PRICE_LIST)
    suspend fun getProductPriceList(): Response<ResponseList<Product_Price_List>>

    @GET(URL_PRODUCT_PRICE)
    suspend fun getProductPrice(@Query("prod_Id") prod_Id: Int): Response<ResponseSingle<Product_Price_List>>
    @GET(URL_PRODUCT_LAST_PRICE)
    suspend fun product_getLastPrice(@Query("prod_id") prod_Id: Int, @Query("PriceCode") priceCode: String,
                                     @Query("uomId") uomId: Int, @Query("curCode") curCode: String): Response<ResponseSingle<Product_Price_List>>

    //////////////////////////////////////////////////////////////////////////////////////////
    ///// CUSTOMERS
    //////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_ALL_CUSTOMERS)
    suspend fun getAllCustomers(@Query("sm_Id") sm_Id: Int, @Query("term") term: String): Response<ResponseList<Customer>>

    @GET(URL_GET_ALL_CUSTOMERS_BY_TERM)
    suspend fun getCustomers_ByTerm(@Query("sm_Id") sm_Id: Int, @Query("Term") term: String, @Query("mntTypeCode") mntTypeCode: String): Response<ResponseList<Customer>>

    @GET(URL_CUSTOMERS_ON_PAGES)
    suspend fun customers_OnPages(@Query("sm_Id") sm_Id: Int?,
                                  @Query("org_Id") org_Id: Int?,
                                  @Query("term") term: String,
                                  @Query("page") page: Int,
                                  @Query("pageSize") pageSize: Int): Response<ResponseList<Customer>>
    @GET(URL_SCHEDULE_CUSTOMERS)
    suspend fun customers_getSchedule(@Query("sm_Id") sm_Id: Int, @Query("term") term: String): Response<ResponseList<Customer>>

    @GET(URL_SCHEDULE_CUSTOMERS_ON_PAGES)
    suspend fun customers_getScheduleOnPages(@Query("sm_Id") sm_Id: Int,
                                             @Query("term") term: String,
                                             @Query("page") page: Int,
                                             @Query("pageSize") pageSize: Int): Response<ResponseList<Customer>>

    @GET(URL_PLACES_CUSTOMERS)
    suspend fun customers_getPlaces(@Query("sm_Id") sm_Id: Int?,
                                    @Query("cyDate") cyDate: String): Response<ResponseList<Customer>>

    @GET(URL_CUSTOMER_STATUS)
    suspend fun customers_getStatus(@Query("cu_Id") cu_Id: Int): Response<ResponseSingle<CustomerStatus>>
    //////////////////////////////////////////////////////////////////////////////////////////
    ////
    //////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_CUSTOMERS_BY_ORG)
    suspend fun customer_GetByOrg(@Query("sm_Id") sm_Id: Int?, @Query("org_Id") org_Id: Int?, @Query("term") term: String): Response<ResponseList<Customer>>
    @GET(URL_CUSTOMER_BY_Id)
    suspend fun getCustomer_ById(@Query("cu_Id") cu_Id: Int): Response<ResponseSingle<Customer>>

    @GET(URL_CUSTOMER_AgeDebit)
    suspend fun customer_getAgeDebit(@Query("cu_Id") cu_Id: Int?) : Response<ResponseSingle<Customer>>

    @POST(URL_INSERT_CUSTOMER)
    suspend fun insertCustomer(@Body baseEo: Customer): Response<ResponseSingle<Customer>>

    //////// Customer Group
    @GET(URL_ALL_CUSTOMER_GROUP)
    suspend fun getCustomerGroups(@Query("term") term: String): Response<ResponseList<Customer_Group>>
    @GET(URL_ALL_CUSTOMER_CATEGORY)
    suspend fun customerCat_GetByTerm(@Query("term") term: String): Response<ResponseList<Customer_Category>>
    ////////CUSTOMER PAYMENT TYPE
    @GET(URL_ALL_CPT)
    suspend fun getCPT_ById(@Query("term") term: String): Response<ResponseList<Customer_Payment_Type>>

    //////////////////////////////////////////////////////////////////////////////////////////
    /// CURRENCY
    //////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_ALL_CURRENCIES)
    suspend fun getAllCurrencies(): Response<ResponseList<Currency>>

    @GET(URL_CURRENCY_BY_CLIENT_ID)
    suspend fun getCurrencyByClientId(@Query("cl_Id") cl_Id: Int): Response<ResponseSingle<Currency>>

    @GET(URL_CURRENCY_BY_CODE)
    suspend fun getCurrencyByCode(@Query("cr_symbol") cr_symbol: String): Response<ResponseSingle<Currency>>
    @GET(URL_ALL_CURRENCIES_RATE)
    suspend fun getCurrencyRate(): Response<ResponseList<Currency_Rate>>

    @GET(URL_CURRENT_CURRENCY_RATE)
    suspend fun getCurrencyLatestRate(@Query("cr_Id") cr_Id: Int): Response<ResponseSingle<Currency_Rate>>

    @GET(URL_BRAND_GET_BY_TERM)
    suspend fun getBrandByTerm(@Query("Term") term: String?): Response<ResponseList<Product_Brand>>

    @GET(URL_BRNAD_ON_PAGES)
    suspend fun brand_OnPages(@Query("Term") term: String?,
                              @Query("page") page: Int,
                              @Query("pageSize") pageSize: Int): Response<ResponseList<Product_Brand>>
    @GET(URL_CATEGORY_GET_BY_TERM)
    suspend fun getCategoriesByTerm(@Query("Term") term: String?): Response<ResponseList<Product_Category>>
    @GET(URL_CATEGORY_ON_PAGES)
    suspend fun getCategoriesOnPages(@Query("Term") term: String?,
                                     @Query("page") page: Int,
                                     @Query("pageSize") pageSize: Int): Response<ResponseList<Product_Category>>
    @GET(URL_ALL_REGION)
    suspend fun getAllRegions(): Response<ResponseList<Region>>

    @GET(URL_ALL_SALESMAN_BY_CODE)
    suspend fun salesmanGetByCode(@Query("pda_code") pda_code: String): Response<ResponseSingle<Salesman>>
    @GET(URL_GET_SALESMAN_BY_USER)
    suspend fun salesmanGetByUser(@Query("UserId") userId: Int): Response<ResponseSingle<Salesman>>
    @GET(URL_GET_CLIENT)
    suspend fun client_Get(): Response<ResponseSingle<Client>>
    @GET(URL_ALL_SALESMAN)
    suspend fun salesmanGetAll(): Response<ResponseList<Salesman>>

    @GET(URL_ALL_SALESMAN_CUSTOMERS)
    suspend fun getAllSalesmanCustomers(): Response<ResponseList<Salesman_Customer>>

    @GET(URL_SALESMAN_SUMMARY)
    suspend fun getSalesmanSummary(@Query("sm_Id") sm_Id: Int?,
                                   @Query("selDate") selDate: String): Response<ResponseSingle<SalesmanSummary>>

    @GET(URL_SALESMAN_HSA_SALES_PLAN)
    suspend fun salesmanHasSalesPlan(@Query("sm_Id") sm_Id: Int): Response<ResponseSingle<Salesman>>

    @GET(URL_USERS_DISC)
    suspend fun getUserDisc(@Query("userId") userId: Int,
                            @Query("discRange") discRange: String) : Response<ResponseSingle<UsersDiscounts>>

    @GET(URL_UOM_PRODUCT)
    suspend fun uom_GetByProduct(@Query("prod_Id") prod_Id: Int): Response<ResponseList<UnitConvertion>>
    //////////////////////////////////////////////////////////////////////////////////////////
    // VOUCHER
    //////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_VOUCHER_BY_CODE)
    suspend fun getVoucherByCode(@Query("vo_code") vo_code: String): Response<ResponseSingle<Voucher>>

    //////////////////////////////////////////////////////////////////////////////////////////
    //// PRICE CATEGORY
    //////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_PRICE_CAT_BY_All)
    suspend fun getPriceCatAll(): Response<ResponseList<PriceCategory>>
    @GET(URL_PRICE_CAT_BY_SALESMAN)
    suspend fun getPriceCatBySalesman(@Query("sm_Id") sm_Id: Int): Response<ResponseList<PriceCategory>>
    @GET(URL_PRICE_CAT_BY_ID)
    suspend fun getPriceCategoryById(@Query("prc_Id") prc_Id: Int): Response<ResponseSingle<PriceCategory>>

    //////////////////////////////////////////////////////////////////////////////////////////
    //// DISCOUNT
    //////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_DISCOUNT_BY_PRODUCT)
    suspend fun discount_GetCurrent(@Query("pr_Id") pr_Id: Int,
                                    @Query("CurrentDate") currentDate: LocalDate,
                                    @Query("org_Id") org_Id: Int?,
                                    @Query("price_cat_code") price_cat_code: String): Response<ResponseSingle<Discount>>

    @GET(URL_WAREHOUSE_GET_ALL)
    suspend fun warehouse_GetAll() : Response<ResponseList<Warehouse>>

    @GET(URL_WAREHOUSE_GET_BY_SALESMAN)
    suspend fun warehouse_GetSalesman(@Query("sm_Id") sm_Id: Int) : Response<ResponseList<Warehouse>>

    @GET(URL_LOCATION_GET_BY_WHS)
    suspend fun location_GetByWhs(@Query("whsId") whsId: Int) : Response<ResponseList<Loc>>
    //////////////////////////////////////////////////////////////////////////////////////////
    //// DELIVERY
    //////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_ALL_DELIVERY_BY_SALESMANID)
    suspend fun getDelivery_BySalesmanId(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?): Response<ResponseList<Delivery>>

    @GET(URL_ALL_DELIVERY_ON_PAGES)
    suspend fun getDelivery_OnPages(@Query("sm_Id") sm_Id: Int,
                                    @Query("term") term: String,
                                    @Query("page") page: Int,
                                    @Query("pageSize") pageSize: Int): Response<ResponseList<Delivery>>

    @GET(URL_DELIVERY_BY_ID)
    suspend fun getDeliveryById(@Query("dl_Id") dl_Id: Int?): Response<ResponseSingle<Delivery>>

    @POST(URL_DELIVERY_UPDATE)
    suspend fun updateDelivery(@Body baseEo: Delivery): Response<ResponseSingle<Delivery>>

    @GET(URL_ALL_DELIVERY_DETAILS)
    suspend fun getAllDeliveryDetails(@Query("dl_Id") dl_Id: Int): Response<ResponseList<Delivery_Items>>

    // for Check load
    @GET(URL_ALL_CHECK_LOAD)
    suspend fun getAllCheckLoad(@Query("sm_Id") sm_Id: Int): Response<ResponseList<Check_Load>>

    @GET(URL_ALL_CHECK_LOAD_DETAILS)
    suspend fun getAllCheckLoadDetails(@Query("sm_Id") sm_Id: Int): Response<ResponseList<Check_Load_Details>>

    //////////////////////////////////////////////////////////////////////////////////////////
    ///// Maintenance
    //////////////////////////////////////////////////////////////////////////////////////////
    @POST(URL_SAVE_MNTS)
    suspend fun insertMnts(@Body baseEo: Mnts) : Response<ResponseSingle<Mnts>>
    @GET(URL_MNTS_GET_BY_ID)
    suspend fun getMntById(@Query("id") id: Int) : Response<ResponseSingle<Mnts>>
    @GET(URL_MNTS_PAGES)
    suspend fun mnts_OnPages(@Query("sm_Id") sm_Id: Int,
                             @Query("term") term: String,
                             @Query("page") page: Int,
                             @Query("pageSize") pageSize: Int) : Response<ResponseList<Mnts>>
    @GET(URL_MNTS_TYPE)
    suspend fun getMntTyps() : Response<ResponseList<MntType>>
    @GET(URL_MNTS_STATUS)
    suspend fun getMntStatus() : Response<ResponseList<MntStatus>>
    @GET(URL_MNTS_REG)
    suspend fun getRegMnt(@Query("term") term: String) : Response<ResponseList<RegMnt>>
    @GET(URL_MNTS_INV)
    suspend fun getInvoicesForMnt(@Query("cu_Id") cu_Id: Int?, @Query("term") term: String) : Response<ResponseList<Sale>>
    @GET(URL_MNTS_WARRANTY)
    suspend fun getWarranty(@Query("cu_Id") cu_Id: Int?, @Query("term") term: String) : Response<ResponseList<Warranty>>
    //////////////////////////////////////////////////////////////////////////////////////////
    ///// SALES
    //////////////////////////////////////////////////////////////////////////////////////////
    @POST(URL_ADD_SALE)
    suspend fun insertInvoice(@Body baseEo: Sale) : Response<ResponseSingle<Sale>>

    @GET(URL_SALE_BY_ID)
    suspend fun getInvoiceById(@Query("sl_Id") sl_Id: Int) : Response<ResponseSingle<Sale>>

    @GET(URL_SALES_FOR_SALESMAN)
    suspend fun getInvoices(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?) : Response<ResponseList<Sale>>

    @GET(URL_SALES_ON_PAGES)
    suspend fun sales_OnPages(@Query("sm_Id") sm_Id: Int,
                              @Query("term") term: String,
                              @Query("page") page: Int,
                              @Query("pageSize") pageSize: Int) : Response<ResponseList<Sale>>
    @GET(URL_INVENTORY_ON_SEARCH)
    suspend fun InvDoc_SearchOnPages(@Query("term") term: String,
                                    @Query("whsId") whsId: Int,
                                    @Query("sotType") sotType: String,
                                    @Query("page") page: Int,
                                    @Query("pageSize") pageSize: Int) : Response<ResponseList<InventoryDoc>>
    @GET(URL_INVENTORY_LINES_ITEMS)
    suspend fun InvDocLinessByDocEntry(@Query("docEntry") docEntry: Int,
                                       @Query("term") term: String,
                                       @Query("whsId") whsId: Int,
                                       @Query("sotType") sotType: String,
                                       @Query("page") page: Int,
                                       @Query("pageSize") pageSize: Int) : Response<ResponseList<InventoryDocLines>>

    //For Stock-in
    @GET(URL_INVENTORY_IN_ON_SEARCH)
    suspend fun InvDocIn_SearchOnPages(@Query("term") term: String,
                                     @Query("whsId") whsId: Int,
                                     @Query("sotType") sotType: String,
                                     @Query("page") page: Int,
                                     @Query("pageSize") pageSize: Int) : Response<ResponseList<InventoryDoc>>
    @GET(URL_INVENTORY_IN_LINES_ITEMS)
    suspend fun InvDocInLinessByDocEntry(@Query("docEntry") docEntry: Int,
                                       @Query("term") term: String,
                                       @Query("whsId") whsId: Int,
                                       @Query("sotType") sotType: String,
                                       @Query("page") page: Int,
                                       @Query("pageSize") pageSize: Int) : Response<ResponseList<InventoryDocLines>>

    @GET(URL_SALES_DELETE)
    suspend fun deleteInvoice(@Query("sl_Id") sl_Id: Int) : Response<ResponseSingle<String>>

    //////////////////////////////////////////////////////////////////////////////////////////
    // Sale Return
    //////////////////////////////////////////////////////////////////////////////////////////
    @POST(URL_ADD_SALE_RETURN)
    suspend fun insertReturn(@Body baseEo: Sale_Return) : Response<ResponseSingle<Sale_Return>>

    @GET(URL_SALE_RETURN_BY_ID)
    suspend fun getSaleReturnById(@Query("sr_Id") sr_Id: Int) : Response<ResponseSingle<Sale_Return>>

    @GET(URL_SALE_RETURN_FOR_SALESMAN)
    suspend fun getReturn(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?) : Response<ResponseList<Sale_Return>>

    @GET(URL_SALE_RETURN_ON_PAGES)
    suspend fun return_OnPages(@Query("sm_Id") sm_Id: Int,
                               @Query("term") term: String?,
                               @Query("page") page: Int,
                               @Query("pageSize") pageSize: Int) : Response<ResponseList<Sale_Return>>

    @GET(URL_SALE_RETURN_DELETE)
    suspend fun deleteReturn(@Query("sr_Id") sr_Id: Int) : Response<ResponseSingle<String>>

    @GET(URL_SALE_RETURN_ITEMS)
    suspend fun getReturnItemsByReturnId(@Query("sr_Id") sr_Id: Int) : Response<ResponseList<Sale_Return_Items>>

    //////////////////////////////////////////////////////////////////////////////////////////
    // Receivable
    //////////////////////////////////////////////////////////////////////////////////////////
    @POST(URL_ADD_RECEIVABLE)
    suspend fun insertReceivable(@Body baseEo: Receivable) : Response<ResponseSingle<Receivable>>
    @GET(URL_RECEIVABLE_BY_ID)
    suspend fun getReceivableById(@Query("py_Id") py_Id: Int) : Response<ResponseSingle<Receivable>>
    @GET(URL_RECEIVABLE_FOR_CUSTOMERS)
    suspend fun getReceivable(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?) : Response<ResponseList<Receivable>>
    @GET(URL_RECEIVABLE_ON_PAGES)
    suspend fun receipt_OnPages(@Query("sm_Id") sm_Id: Int,
                                @Query("term") term: String?,
                                @Query("page") page: Int,
                                @Query("pageSize") pageSize: Int) : Response<ResponseList<Receivable>>
    @GET(URL_RECEIVABLE_DELETE)
    suspend fun deleteReceivable(@Query("rcv_Id") rcv_Id: Int) : Response<ResponseSingle<String>>

    //////////////////////////////////////////////////////////////////////////////////////////
    // Payable
    //////////////////////////////////////////////////////////////////////////////////////////
    @POST(URL_ADD_PAYABLE)
    suspend fun insertPayable(@Body baseEo: Payable) : Response<ResponseSingle<Payable>>
    @GET(URL_PAYABLE_BY_ID)
    suspend fun getPayableById(@Query("py_Id") py_Id: Int) : Response<ResponseSingle<Payable>>
    @GET(URL_PAYABLE_FOR_CUSTOMERS)
    suspend fun getPayable(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?) : Response<ResponseList<Payable>>

    @GET(URL_PAYABLE_ON_PAGES)
    suspend fun payable_OnPages(@Query("sm_Id") sm_Id: Int,
                                @Query("term") term: String?,
                                @Query("page") page: Int,
                                @Query("pageSize") pageSize: Int) : Response<ResponseList<Payable>>

    @GET(URL_PAYABLE_DELETE)
    suspend fun deletePayable(@Query("py_Id") py_Id: Int) : Response<ResponseSingle<String>>

    ////////////////////////////////////////////////////////
    ///////////////////// SALES ORDER //////////////////////
    ///////////////////////////////////////////////////////
    @POST(URL_ADD_ORDER)
    suspend fun insertOrder(@Body order: Sale_Order) : Response<ResponseSingle<Sale_Order>>

    @POST(URL_UPDATE_ORDER)
    suspend fun updateOder(@Body order: Sale_Order) : Response<ResponseSingle<Sale_Order>>

    @GET(URL_ORDER_BY_ID)
    suspend fun getOrderById(@Query("so_Id") so_Id: Int) : Response<ResponseSingle<Sale_Order>>

    @GET(URL_ORDER_BY_CUSTOMERS)
    suspend fun getOrderByCustomerId(@Query("cu_Id") cu_Id: Int) : Response<ResponseList<Sale_Order>>

    @GET(URL_ORDER_BY_SALESMAN)
    suspend fun getOrders(@Query("sm_Id") sm_Id: Int, @Query("cu_Id") cu_Id: Int?, @Query("vo_code") vo_code: String) : Response<ResponseList<Sale_Order>>

    @GET(URL_ORDER_BY_ONPAGES)
    suspend fun getOrdersOnPages(@Query("sm_Id") sm_Id: Int,
                                 @Query("cu_Id") cu_Id: Int?,
                                 @Query("vo_code") vo_code: String,
                                 @Query("term") term: String,
                                 @Query("page") page: Int,
                                 @Query("pageSize") pageSize: Int) : Response<ResponseList<Sale_Order>>

    @GET(URL_ORDER_DELETE)
    suspend fun deleteOrder(@Query("so_Id") so_Id: Int) : Response<ResponseSingle<String>>

    @GET(URL_ORDER_ITEMS)
    suspend fun getOrderItemsByOrderId(@Query("so_Id") so_Id: Int) : Response<ResponseList<Sale_Order_Items>>

    // Inventory Stock-in
    @POST(URL_ADD_STOCK_IN)
    suspend fun insertStockIn(@Body baseEo: Stockin) : Response<ResponseSingle<Stockin>>

    @GET(URL_STOCK_IN_BY_ID)
    suspend fun getStockInById(@Query("sin_Id") sin_Id: Int) : Response<ResponseSingle<Stockin>>

    @GET(URL_STOCK_IN_ON_PAGES)
    suspend fun getStockInOnPages(@Query("sm_Id") sm_Id: Int,
                                  @Query("term") term: String,
                                  @Query("page") page: Int,
                                  @Query("pageSize") pageSize: Int) : Response<ResponseList<Stockin>>

    // Inventory Stock-out
    @POST(URL_ADD_STOCK_OUT)
    suspend fun saveStockOut(@Body baseEo: Stockout) : Response<ResponseSingle<Stockout>>

    @GET(URL_STOCK_OUT_BY_ID)
    suspend fun getStockOutById(@Query("sout_Id") sout_Id: Int) : Response<ResponseSingle<Stockout>>

    @GET(URL_STOCK_OUT_ON_PAGES)
    suspend fun getStockOutOnPages(@Query("sm_Id") sm_Id: Int,
                                   @Query("term") term: String,
                                   @Query("page") page: Int,
                                   @Query("pageSize") pageSize: Int) : Response<ResponseList<Stockout>>

    /////////////////////////////////////////////////////
    ///////////////////// Lookup ////////////////////
    /////////////////////////////////////////////////////
    @GET(URL_LOOKUP_GET_BY_ENTITY)
    suspend fun loockup_getByEntity(@Query("entity_name") entity_name: String) : Response<ResponseList<Lookups>>
    /////////////////////////////////////////////////////
    ///////////////////// Call Cycle ////////////////////
    /////////////////////////////////////////////////////
    @GET(URL_CALL_CYCLE_GET_BY_ID)
    suspend fun call_cycle_GetById(@Query("cy_Id") cy_Id: Int) : Response<ResponseSingle<Call_Cycle>>

    @GET(URL_CALL_CYCLE_GET_ON_PAGES)
    suspend fun call_cycle_GetByOnPages(@Query("sm_Id") sm_Id: Int,
                                        @Query("term") term: String?,
                                        @Query("page") page: Int,
                                        @Query("pageSize") pageSize: Int) : Response<ResponseList<Call_Cycle>>

    @POST(URL_SAVE_OR_UPDATE_CALL_CYCLE)
    suspend fun callCycle_SaveOrUpdate(@Body baseEo: Call_Cycle) : Response<ResponseSingle<Call_Cycle>>
    /////////////////////////////////////////////////////
    ///////////////////// TRANSFER //////////////////////
    /////////////////////////////////////////////////////
    @POST(URL_ADD_TRANSFER)
    suspend fun transfer_SaveOrUpdate(@Body baseEo: Transfer): Response<ResponseSingle<Transfer>>
    @GET(URL_TRANSFER_GET_BY_ID)
    suspend fun transfer_getById(@Query("tr_Id") tr_Id: Int): Response<ResponseSingle<Transfer>>
    @GET(URL_TRANSFER_GET_BY_User)
    suspend fun transfer_getByUserId(@Query("userId") userId: Int): Response<ResponseList<Transfer>>

    @GET(URL_TRANSFER_GET_ON_PAGES)
    suspend fun transfer_getOnPages(@Query("userId") userId: Int,
                                    @Query("term") term: String?,
                                    @Query("page") page: Int,
                                    @Query("pageSize") pageSize: Int): Response<ResponseList<Transfer>>

    @GET(URL_TRANSFER_GET_BY_MASTER_ID)
    suspend fun transfer_GetItemsByMasterId(@Query("tr_Id") tr_Id: Int) : Response<ResponseList<Transfer_Items>>

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// REPORTS API
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @GET(URL_CASHBOOK_STATEMENT_ON_PAGES)
    suspend fun cashbookStatement_OnPages(@Query("userId") userId: Int,
                                          @Query("dtFrom") dtFrom: String?,
                                          @Query("dtTo") dtTo: String?,
                                          @Query("page") page: Int,
                                          @Query("pageSize") pageSize: Int): Response<ResponseList<CashbookStatement>>

    @GET(URL_SALES_STATEMENT_ON_PAGES)
    suspend fun salesStatement_OnPages(@Query("userId") userId: Int,
                                       @Query("dtFrom") dtFrom: String?,
                                       @Query("dtTo") dtTo: String?,
                                       @Query("page") page: Int,
                                       @Query("pageSize") pageSize: Int): Response<ResponseList<SalesStatement>>

    @GET(URL_INVENTORY_STATEMENT_ON_PAGES)
    suspend fun inventoryStatement_OnPages(@Query("wr_Id") wr_Id: Int,
                                           @Query("dtTo") dtTo: String?,
                                           @Query("page") page: Int,
                                           @Query("pageSize") pageSize: Int): Response<ResponseList<StockStatement>>


    @GET(URL_CUSTOMER_STATEMENT_ON_PAGES)
    suspend fun cusotmerStatement_OnPages(@Query("userId") userId: Int,
                                          @Query("cu_Id") cu_Id: Int,
                                          @Query("dtFrom") dtFrom: String?,
                                          @Query("dtTo") dtTo: String?,
                                          @Query("page") page: Int,
                                          @Query("pageSize") pageSize: Int): Response<ResponseList<CustomerStatement>>

    //================ Dashboard functions
    @GET(URL_DASHBOARD_TOTAL_CUSTOMER)
    suspend fun getDashboardTotCustomer(@Query("sm_Id") sm_Id: Int,
                                        @Query("dtFrom") dtFrom: String,
                                        @Query("dtTo") dtTo: String): Response<ResponseSingle<sm_dash1>>
    @GET(URL_DASHBOARD_SALES_PLAN)
    suspend fun getDashboardSalesPlanning(@Query("sm_Id") sm_Id: Int,
                                        @Query("PlanId") planId: Int): Response<ResponseSingle<sm_dash2>>

    @GET(URL_SALES_PLAN)
    suspend fun getSalesPlan(@Query("sm_Id") sm_Id: Int): Response<ResponseList<Lookups>>

    @Multipart
    @POST(URL_UPLOAD_FILE)
    suspend fun uploadFile(@Part description : RequestBody, @Part photo : MultipartBody.Part) : Response<ResponseBody>

    companion object{
        operator fun invoke(connectivityInterceptor: ConnectivityInterceptor) : ApiService{

//            val requestInterceptor = Interceptor{ chain ->
//                val url = chain.request()
//                    .newBuilder()
//                    .url(BASE_URL)
//                    .addQueryParameter("key", API_KEY)
//                    .build()
//                val request = chain.request()
//                    .newBuilder()
//                    .url(url)
//                    .build()
//
//                return@Interceptor chain.proceed(request)
//            }
            // define okHttpclient for checking if connection is available or not
            val okHttpclient = OkHttpClient.Builder()
                .addInterceptor(connectivityInterceptor)
                .connectTimeout(120, TimeUnit.SECONDS)
                .build()

            val client = getUnsafeOkHttpClient()
            return Retrofit.Builder()
                //.client(okHttpclient) // for checking if connection is avialable or not
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)
        }

        fun getUnsafeOkHttpClient1(): OkHttpClient.Builder? {
            return try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
                )

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { hostname, session -> true }
                builder
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}