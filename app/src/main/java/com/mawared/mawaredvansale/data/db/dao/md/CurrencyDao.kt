package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Currency

@Dao
interface CurrencyDao : BaseDao<Currency> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Currency")
    fun getAll(): LiveData<List<Currency>>

    /**
     * Get single currency data from the Data table.
     */
    @Query("SELECT * FROM Currency WHERE cr_id=:id")
    fun getById(id: Int): LiveData<Currency>

    /**
     * Get single currency data from the Data table.
     */
    @Query("SELECT * FROM Currency WHERE cr_symb = :symbol LIMIT 1")
    fun getBySymbol(symbol: String): LiveData<Currency>
}