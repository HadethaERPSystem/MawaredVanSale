package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.Order


@Dao
interface OrderDao : BaseDao<Order> {
    @Query("SELECT * FROM [Order] WHERE od_Id = :id")
    fun getOrder(id: Int): List<Order>

    @Insert
    fun addOrder(baseEo: Order)

}