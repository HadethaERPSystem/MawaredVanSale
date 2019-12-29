package com.mawared.mawaredvansale.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mawared.mawaredvansale.data.db.dao.md.*
import com.mawared.mawaredvansale.data.db.dao.sales.SaleDao
import com.mawared.mawaredvansale.data.db.dao.sales.Sale_DetailsDao
import com.mawared.mawaredvansale.data.db.dao.security.MenuDao
import com.mawared.mawaredvansale.data.db.dao.security.UserDao
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.data.db.entities.security.User

@Database(
    entities = [User::class, Sale::class, Sale_Items::class, Menu::class, Call_Cycle::class, Currency::class, Currency_Rate::class,
                Customer::class, Customer_Group::class, Customer_Price_List::class, Product::class, Product_Brand::class, Product_Category::class,
                Product_Price_List::class, Region::class, Salesman::class, Salesman_Customer::class],
    version = 12, exportSchema = false
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    // Security DAO
    abstract fun getUserDao(): UserDao
    abstract fun getMenuDao(): MenuDao
    // Sale DAO
    abstract fun getSaleDao(): SaleDao
    abstract fun getSaleDetailDao(): Sale_DetailsDao
    // Delivery DAO

    //abstract fun getDeliveryDao(): DeliveryDao
    // Master Data DAO
    abstract fun getCall_CycleDao(): Call_CycleDao
    abstract fun getCurrencyDao(): CurrencyDao
    abstract fun getCurrencyRateDao(): CurrencyRateDao
    abstract fun getCustomerGroupDao(): Customer_GroupDao
    abstract fun getCustomerPriceList(): Customer_Price_ListDao
    abstract fun getCustomerDao(): CustomerDao
    abstract fun getProductDao(): ProductDao
    abstract fun getProductBrandDao(): Product_BrandDao
    abstract fun getProductCategoryDao(): Product_CategoryDao
    abstract fun getProductPriceList(): Product_Price_ListDao
    abstract fun getRegionDao(): RegionDao
    abstract fun getSalesmanDao(): SalesmanDao
    abstract fun getSalesmanCustomerDao(): Salesman_CustomerDao

    companion object{

        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(
            LOCK
        ){
            instance
                ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mmobilepos.db"
            ).build()
    }
}