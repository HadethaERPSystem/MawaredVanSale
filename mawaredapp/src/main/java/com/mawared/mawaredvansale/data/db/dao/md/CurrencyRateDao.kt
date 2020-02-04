package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Currency_Rate
import org.threeten.bp.LocalDate

@Dao
interface CurrencyRateDao : BaseDao<Currency_Rate> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Currency_Rate")
    fun getAll(): LiveData<List<Currency_Rate>>

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Currency_Rate WHERE cr_code_to = :cr_code AND cr_date <= :date ORDER BY cr_date DESC LIMIT 1")
    fun getLastRatel(cr_code: String, date: LocalDate): LiveData<List<Currency_Rate>>
}