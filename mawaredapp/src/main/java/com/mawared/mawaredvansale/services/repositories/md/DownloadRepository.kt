package com.mawared.mawaredvansale.services.repositories.md

import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.sales.*
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseList
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadRepository(
    private val api: ApiService,
    private val db: AppDatabase
) : SafeApiRequest()  {

    // Products
    //=====================================================================================
    private suspend fun getAllProducts(term: String, warehouseId: Int?, priceCode: String) : ResponseList<Product>{
        return apiRequest { api.products_GetByTerm(term, warehouseId, priceCode) }
    }

    private suspend fun insertProduct(baseEoList: List<Product>) = db.getProductDao().insert(baseEoList)

    suspend fun downloadProduct(term: String, warehouseId: Int?, priceCode: String){
        withContext(Dispatchers.IO){
            val response = getAllProducts(term, warehouseId, priceCode)
            if(response .isSuccessful){
                if(response.data != null)
                insertProduct(response.data)
            }
        }
    }

    // Product Brand
    //===========================================================
    private suspend fun getAllBrand(): ResponseList<Product_Brand>{
        return apiRequest { api.getAllBrand() }
    }

    private suspend fun insertBrand(baseEoList: List<Product_Brand>) = db.getProductBrandDao().insert(baseEoList)

    suspend fun downloadProductBrand(){
        withContext(Dispatchers.IO){
            val response = getAllBrand()
            if(response.isSuccessful){
                if(response.data != null){
                    insertBrand(response.data)
                }
            }
        }
    }

    // Product Category
    //===========================================================
    private suspend fun getAllCategories(): ResponseList<Product_Category>{
        return apiRequest { api.getAllCategories() }
    }

    private suspend fun insertCategory(baseEoList: List<Product_Category>) = db.getProductCategoryDao().insert(baseEoList)

    suspend fun downloadProductCategory(){
        withContext(Dispatchers.IO){
            val response = getAllBrand()
            if(response.isSuccessful){
                if(response.data != null){
                    insertBrand(response.data)
                }
            }
        }
    }
    // Product price list
    //====================================================================================
    private suspend fun getProductPriceList() : ResponseList<Product_Price_List>{
        return apiRequest { api.getProductPriceList() }
    }

    private suspend fun insertProductPrice(baseEoList: List<Product_Price_List>) = db.getProductPriceList().insert(baseEoList)

    suspend fun downloadProductPriceList(){
        withContext(Dispatchers.IO){
            val response = getProductPriceList()
            if(response.isSuccessful)
                if(response.data != null)
                    insertProductPrice(response.data)
        }
    }

    // Customer
    //========================================================================================
    private suspend fun getCustomers(sm_Id: Int): ResponseList<Customer>{
        return apiRequest { api.getAllCustomers(sm_Id) }
    }

    private suspend fun insertCustomers(baseEoList: List<Customer>) = db.getCustomerDao().insert(baseEoList)

    suspend fun downloadCustomers(sm_Id: Int){
        withContext(Dispatchers.IO){
            val response = getCustomers(sm_Id)
            if(response.isSuccessful){
                if(response.data != null){
                    insertCustomers(response.data)
                }
            }
        }
    }

    // Currency
    private suspend fun getAllCurrencies(): ResponseList<Currency>{
       return apiRequest { api.getAllCurrencies() }
    }

    private suspend fun insertCurrencies(baseEoList: List<Currency>) = db.getCurrencyDao().insert(baseEoList)

    suspend fun downloadCurrency(){
        withContext(Dispatchers.IO){
            val response = getAllCurrencies()
            if(response.isSuccessful){
                if(response.data != null){
                    insertCurrencies(response.data)
                }
            }
        }
    }

    // Currency Rate
    // =============================================================
    private suspend fun getCurrencyRate(): ResponseList<Currency_Rate>{
        return apiRequest { api.getCurrencyRate() }
    }

    private suspend fun insertCurrencyRate(baseEoList: List<Currency_Rate>) = db.getCurrencyRateDao().insert(baseEoList)

    suspend fun downloadCurrencyRate(){
        withContext(Dispatchers.IO){
            val response = getCurrencyRate()
            if(response.isSuccessful){
                if(response.data != null){
                    insertCurrencyRate(response.data)
                }
            }
        }
    }

    // Regions
    //===========================================================
    private suspend fun getAllRegions(): ResponseList<Region>{
        return apiRequest { api.getAllRegions() }
    }

    private suspend fun insertRegions(baseEoList: List<Region>) = db.getRegionDao().insert(baseEoList)

    suspend fun downloadRegions(){
        withContext(Dispatchers.IO){
            val response = getAllRegions()
            if(response.isSuccessful){
                if(response.data != null){
                    insertRegions(response.data)
                }
            }
        }
    }
    // Salesmand
    //===========================================================
    private suspend fun getAllSalesman(pda_code: String): ResponseSingle<Salesman>{
        return apiRequest { api.salesmanGetByCode(pda_code) }
    }

    private suspend fun insertSalesman(baseEo: Salesman) = db.getSalesmanDao().insert(baseEo)

    suspend fun downloadSalesman(pda_code: String){
        withContext(Dispatchers.IO){
            val response = getAllSalesman(pda_code)
            if(response.isSuccessful){
                if(response.data != null){
                    insertSalesman(response.data)
                }
            }
        }
    }

    // Salesman Customers
    //===========================================================
    suspend fun getAllSalesmanCustomers(): ResponseList<Salesman_Customer>{
        return apiRequest { api.getAllSalesmanCustomers() }
    }

    // Sales Management api
    suspend fun insertSale(sale: Sale): ResponseSingle<Sale>{
        return apiRequest { api.insertInvoice(sale) }
    }

    suspend fun updateSale(sale: Sale): ResponseSingle<Sale>{
        return apiRequest { api.insertInvoice(sale) }
    }

    suspend fun getSaleById(sl_Id: Int) : ResponseSingle<Sale>{
        return apiRequest { api.getInvoiceById(sl_Id) }
    }

//    suspend fun getSalesByCustomerId(cu_Id: Int): ListRecsResponse<Sale>{
//        return apiRequest { api.getInvoiceByCustomerId(cu_Id) }
//    }

    suspend fun getSaleItemsByMasterId(sl_Id: Int): ResponseList<Sale_Items>{
        return apiRequest { api.getInvoiceItemsByInvoiceId(sl_Id) }
    }

    // Sales Order Management api
    suspend fun insertOrder(order: Sale_Order): ResponseSingle<Sale_Order>{
        return apiRequest { api.insertOrder(order) }
    }

    suspend fun updateOrder(order: Sale_Order): ResponseSingle<Sale_Order>{
        return apiRequest { api.updateOder(order) }
    }

    suspend fun getOrderById(so_Id: Int) : ResponseSingle<Sale_Order>{
        return apiRequest { api.getOrderById(so_Id) }
    }

    suspend fun getOrderByCustomerId(cu_Id: Int): ResponseList<Sale_Order>{
        return apiRequest { api.getOrderByCustomerId(cu_Id) }
    }

    suspend fun getOrderItemsByMasterId(so_Id: Int): ResponseList<Sale_Order_Items>{
        return apiRequest { api.getOrderItemsByOrderId(so_Id) }
    }
}