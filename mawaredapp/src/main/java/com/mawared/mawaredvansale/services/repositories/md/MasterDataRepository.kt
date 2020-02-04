package com.mawared.mawaredvansale.services.repositories.md

import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest

class MasterDataRepository(
    private val api: ApiService,
    private val db: AppDatabase
) : SafeApiRequest() {

    fun getAllProducts() = db.getProductDao().getAll()
    fun getProductByTerm(term: String) = db.getProductDao().getByTerm(term)
    fun getProductByBarcode(barcode: String) = db.getProductDao().getByBarcode(barcode)

    fun getAllCurrencies() = db.getCurrencyDao().getAll()
    fun getCurrencyBySymbol(cr_symbol: String) = db.getCurrencyDao().getBySymbol(cr_symbol)
    fun getProductPrice(prod_Id: Int, cr_Id: Int) = db.getProductPriceList().getItemPrice(prod_Id, cr_Id)

    fun getAllCustomers() = db.getCustomerDao().getAll()
}