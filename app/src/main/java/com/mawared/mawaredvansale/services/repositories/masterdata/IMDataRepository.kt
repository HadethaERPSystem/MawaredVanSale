package com.mawared.mawaredvansale.services.repositories.masterdata

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.md.*

interface IMDataRepository {
    // Customer method
    fun getCustomers(sm_Id: Int): LiveData<List<Customer>>
    fun getCustomerById(cu_Id: Int): LiveData<Customer>
    fun insertCustomer(baseEo: Customer): LiveData<Customer>

    // Customer Payment Type
    fun getCptAll(term: String): LiveData<List<Customer_Payment_Type>>

    // Customer Group
    fun getCustomersGroups(term: String): LiveData<List<Customer_Group>>
    // Product method
    fun getProducts(term: String, warehouseId: Int?, priceCode: String): LiveData<List<Product>>
    fun productGetByBarcode(barcode: String, warehouseId: Int?, priceCode: String): LiveData<Product>
    fun getProductById(prod_Id: Int): LiveData<Product>
    // Product Price
    fun getProductPrice(prod_Id: Int): LiveData<Product_Price_List>
    // Currency
    fun getCurrency(cl_Id: Int): LiveData<Currency>

    fun getCurrencyByCode(cr_symbol: String): LiveData<Currency>
    // Currency Rate
    fun getRate(cr_Id: Int): LiveData<Currency_Rate>
    // Salesman
    fun getSalesman(pda_code: String): LiveData<Salesman>

    // Voucher
    fun getVoucherByCode(vo_code: String): LiveData<Voucher>

    fun warehouse_GetAll(): LiveData<List<Warehouse>>
    fun cancelJob()
}