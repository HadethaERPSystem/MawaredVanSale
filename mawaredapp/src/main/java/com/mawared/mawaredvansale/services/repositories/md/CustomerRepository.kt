package com.mawared.mawaredvansale.services.repositories.md

import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseList

class CustomerRepository(
    private val api: ApiService,
    private val db: AppDatabase
) : SafeApiRequest()  {

    fun getAll() = db.getCustomerDao().getAll()
    suspend fun insert(customer: Customer) = db.getCustomerDao().insert(customer)
    suspend fun update(customer: Customer) = db.getCustomerDao().update(customer)
    suspend fun deleteAll() = db.getCustomerDao().deleteAll()

    suspend fun getBySalesman(sm_Id: Int) : ResponseList<Customer> {
        return apiRequest { api.getAllCustomers(sm_Id, "") }
    }
}