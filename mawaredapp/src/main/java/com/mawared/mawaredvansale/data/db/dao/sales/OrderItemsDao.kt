package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.OrderItems

@Dao
interface OrderItemsDao : BaseDao<OrderItems> {
    @Query("SELECT * FROM OrderItems")
    fun getItems(): List<OrderItems>

    @Insert
    fun addItem(baseEo: OrderItems)

    @Query("DELETE FROM OrderItems")
    fun deleteAllItems()
}