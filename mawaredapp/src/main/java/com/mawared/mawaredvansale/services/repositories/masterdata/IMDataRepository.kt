package com.mawared.mawaredvansale.services.repositories.masterdata

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDoc
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDocLines
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.md.Currency
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatus
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState
import org.threeten.bp.LocalDate

interface IMDataRepository {
    val networkState: LiveData<NetworkState>

//    fun fetchCustomerOnPages(sm_Id: Int?, org_Id: Int?, term: String): LiveData<PagedList<Customer>>
//    fun getCustomerNetworkState(): LiveData<NetworkState>
//    fun fetchScheduledCustomerOnPages(sm_Id: Int): LiveData<PagedList<Customer>>
//    fun getScheduleCustomerNetworkState(): LiveData<NetworkState>
    // Items
//    fun fetchItemsOnPages(term: String, priceCode: String): LiveData<PagedList<Product>>
//    fun getItemNetworkState(): LiveData<NetworkState>
//    fun fetchItemsByUserOnPages(term: String, userId: Int, priceCode: String): LiveData<PagedList<Product>>
//    fun getItemUserNetworkState(): LiveData<NetworkState>
//    fun fetchItemsByWarehouseOnPages(term: String, wr_Id: Int, priceCode: String): LiveData<PagedList<Product>>
//    fun getItemWareNetworkState(): LiveData<NetworkState>
    // Customer method
    //suspend fun getCustomer(): LiveData<List<Customer>>
    fun getCustomers(sm_Id: Int, term: String): LiveData<List<Customer>>
    fun getCustomers_ByTerm(sm_Id: Int, term: String, mntTypeCode: String): LiveData<List<Customer>>
    suspend fun getCustomersOnPages(sm_Id: Int, org_Id: Int?,  term: String,  page: Int ): List<Customer>?
    suspend fun getScheduleCustomersOnPages(sm_Id: Int, term: String,  page: Int): List<Customer>?;
    fun customers_getSchedule(sm_Id: Int, term: String): LiveData<List<Customer>>
    fun customers_getPlaces(sm_Id: Int, cyDate: String): LiveData<List<Customer>>
    fun getCustomersByOrg(sm_id: Int, org_Id: Int?, term: String): LiveData<List<Customer>>
    fun getCustomerById(cu_Id: Int): LiveData<Customer>
    fun insertCustomer(baseEo: Customer): LiveData<Customer>
    fun getCustomerStatus(cu_Id: Int): LiveData<CustomerStatus>
    suspend fun customerSaveOrUpdate(baseEo: Customer): ResponseSingle<Customer>
    suspend fun customer_getAgeDebit(cu_Id: Int): Customer?
    // Customer Payment Type
    fun getCptAll(term: String): LiveData<List<Customer_Payment_Type>>

    // Customer Group
    fun getCustomersGroups(term: String): LiveData<List<Customer_Group>>
    // Customer Category
    fun customersCategory_GetByTerm(term: String): LiveData<List<Customer_Category>>
    // Product method
    fun getProducts(term: String, warehouseId: Int?, priceCode: String): LiveData<List<Product>>
    suspend fun getProductForMarket(warehouseId: Int?, priceCode: String, currentDate: LocalDate, org_Id: Int?, cat_Id: Int?, br_Id: Int?, Term: String?, objCode: String, page: Int): List<Product>?
    suspend fun getProductForMarketSO(userId: Int?, priceCode: String, currentDate: LocalDate, org_Id: Int?, cat_Id: Int?, br_Id: Int?, Term: String?, objCode: String, page: Int): List<Product>?
    suspend fun getProductForOffers(warehouseId: Int?, priceCode: String, currentDate: LocalDate, org_Id: Int?, Term: String?, objCode: String, page: Int): List<Product>?
    fun getProductsByPriceTerm(term: String, priceCode: String): LiveData<List<Product>>
    fun getProductsByUserWarehouse(term: String, userId: Int?, priceCode: String): LiveData<List<Product>>
    fun getProductsBySearch(term: String): LiveData<List<Product>>
    fun getProductsByContract(contId: Int?, term: String): LiveData<List<Product>>
    fun getProducts_InvoicesByCustomer(cu_Id: Int, prod_Id: Int, term: String): LiveData<List<Product>>
    fun productGetByBarcode(barcode: String, warehouseId: Int?, priceCode: String): LiveData<Product>
    fun getProductById(prod_Id: Int): LiveData<Product>

    fun  getServicesByTerm(term: String, priceCode: String) : LiveData<List<Servs>>
    // Product Price
    fun getProductPrice(prod_Id: Int): LiveData<Product_Price_List>
    suspend fun product_getLastPrice(prod_Id: Int, PriceCode: String, uomId: Int, curCode: String): Product_Price_List?
    // Currency
    fun getCurrency(cl_Id: Int): LiveData<Currency>

    fun getCurrencyByCode(cr_symbol: String): LiveData<Currency>
    // Currency Rate
    fun getRate(cr_Id: Int): LiveData<Currency_Rate>
    fun brand_GetByTerm(term: String?): LiveData<List<Product_Brand>>
    suspend fun brandOnPages(term: String?, page: Int): List<Product_Brand>?
    fun categories_GetByTerm(term: String?): LiveData<List<Product_Category>>
    suspend fun categoriesOnPages(term: String?, page: Int) : List<Product_Category>?
    // Salesman
    fun getSalesman(pda_code: String): LiveData<Salesman>
    fun salesman_GetAll(): LiveData<List<Salesman>>
    fun salesman_getSummary(sm_Id: Int, selDate: String): LiveData<SalesmanSummary>
    suspend fun salesman_hasSalesPlan(sm_Id: Int): Salesman?
    suspend fun getDisc(userId: Int, discRange: String) : UsersDiscounts?
    suspend fun uom_GetByProduct(prod_Id: Int): List<UnitConvertion>?
    // Voucher
    fun getVoucherByCode(vo_code: String): LiveData<Voucher>
    // Price Category
    fun priceCat_GetAll(): LiveData<List<PriceCategory>>
    fun priceCat_GetBySalesman(sm_Id: Int): LiveData<List<PriceCategory>>
    fun priceCat_GetById(prc_Id: Int): LiveData<PriceCategory>
    fun getDiscountItem(pr_Id: Int, currentDate: LocalDate, org_Id: Int?, price_cat_code: String): LiveData<Discount>
    fun warehouse_GetAll(): LiveData<List<Warehouse>>
    fun warehouse_GetBySalesman(sm_Id: Int): LiveData<List<Warehouse>>
    fun location_GetByWhs(whsId: Int) : LiveData<List<Loc>>
    // Sales plan
    fun getSalesPaln(sm_Id: Int): LiveData<List<Lookups>>
    // Region
    fun getRegions(): LiveData<List<Region>>
    // Lookup
    fun lookup_getByEntity(entity_name: String): LiveData<List<Lookups>>

    // Maintenance
    fun getMntType(): LiveData<List<MntType>>
    fun getMntStatus(): LiveData<List<MntStatus>>
    fun getWaiting_RegMnt(term: String): LiveData<List<RegMnt>>
    fun getInvoicsByTerm(cu_Id: Int?, term: String): LiveData<List<Sale>>
    fun getWarrantyByTerm(cu_Id: Int?, term: String): LiveData<List<Warranty>>
    fun cancelJob()

    // functions for get document for stockout
    suspend fun invoices_SearchOnPages(term: String, whsId: Int, sotType: String, page: Int): List<InventoryDoc>?
    suspend fun saleItemsForStockout(sl_Id: Int, term: String,  whsId: Int, sotType: String, page: Int): List<InventoryDocLines>?

    suspend fun docforStockin_SearchOnPages(term: String, whsId: Int, sotType: String, page: Int): List<InventoryDoc>?
    suspend fun doclinesForStockin(sl_Id: Int, term: String,  whsId: Int, sotType: String, page: Int): List<InventoryDocLines>?
}