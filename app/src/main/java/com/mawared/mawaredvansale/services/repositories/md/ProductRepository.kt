package com.mawared.mawaredvansale.services.repositories.md

import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ListRecsResponse

class ProductRepository(
    private val api: ApiService,
    private val db: AppDatabase
) : SafeApiRequest() {

    fun getAll() = db.getProductDao().getAll()
    fun getByName(term: String) = db.getProductDao().getByTerm(term)
    fun getByBarcode(barcode: String) = db.getProductDao().getByBarcode(barcode)

    suspend fun insert(product: Product) = db.getProductDao().insert(product)
    suspend fun update(product: Product) = db.getProductDao().update(product)

    suspend fun getByAll(term: String, warehouseId: Int?, priceCode: String) : ListRecsResponse<Product> {
        return apiRequest { api.products_GetByTerm(term, warehouseId, priceCode) }
    }

}