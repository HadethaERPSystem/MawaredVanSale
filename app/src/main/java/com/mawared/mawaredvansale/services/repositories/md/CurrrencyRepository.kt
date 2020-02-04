package com.mawared.mawaredvansale.services.repositories.md

import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.md.Currency
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseList

class CurrrencyRepository(private val api: ApiService, private val db: AppDatabase) : SafeApiRequest() {

    fun getAll() = db.getCurrencyDao().getAll()
    fun getCurrencyBySymbol(cr_symbol: String) = db.getCurrencyDao().getBySymbol(cr_symbol)
    suspend fun insert(baseEo: Currency) = db.getCurrencyDao().insert(baseEo)
    suspend fun update(baseEo: Currency) = db.getCurrencyDao().update(baseEo)

    suspend fun getByAll() : ResponseList<Currency> {
        return apiRequest { api.getAllCurrencies() }
    }
}