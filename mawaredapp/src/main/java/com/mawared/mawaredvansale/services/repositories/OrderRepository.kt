package com.mawared.mawaredvansale.services.repositories

import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.sales.Order
import com.mawared.mawaredvansale.data.db.entities.sales.OrderItems

class OrderRepository(private val db: AppDatabase) {

    suspend fun addOrder(order: Order) = db.getOrderDao().upsert(order)
    suspend fun addOrderItem(orderItems: OrderItems) = db.getOrderItemsDao().insert(orderItems)
    suspend fun updateOrderItem(orderItems: OrderItems) = db.getOrderItemsDao().update(orderItems)
    fun getOrder(id: Int) = db.getOrderDao().getOrder(id)
    fun getOrderItems() = db.getOrderItemsDao().getItems()
    suspend fun delete(order: Order) = db.getOrderDao().delete(order)
    suspend fun deleteItem(orderItems: OrderItems) = db.getOrderItemsDao().delete(orderItems)
    fun deleteAllItems() = db.getOrderItemsDao().deleteAllItems()
}