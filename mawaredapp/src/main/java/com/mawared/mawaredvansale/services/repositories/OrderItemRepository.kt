package com.mawared.mawaredvansale.services.repositories

import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.sales.OrderItems

class OrderItemRepository(private val db: AppDatabase) {
    suspend fun addOrderItem(orderItems: OrderItems) = db.getOrderItemsDao().upsert(orderItems)
    fun getOrderItems() = db.getOrderItemsDao().getItems()
    suspend fun delete(orderItems: OrderItems) = db.getOrderItemsDao().delete(orderItems)
    fun deleteAll() = db.getOrderItemsDao().deleteAllItems()
}