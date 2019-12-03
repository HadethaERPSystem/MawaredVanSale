package com.mawared.mawaredvansale.services.repositories

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MenuRepository(
    private val api: ApiService,
    private val db: AppDatabase
) : SafeApiRequest() {

    var menuList : MutableLiveData<List<Menu>> = MutableLiveData()
     suspend fun getMenu() : LiveData<List<Menu>>{
         return withContext(Dispatchers.IO){
             db.getMenuDao().getAll()
         }
    }

    fun getLocalMenu(res: Resources): MutableLiveData<List<Menu>>{

        //var id = res.getIdentifier("ic_orders", "mipmap", ctx.packageName)
        val mList = listOf(
            Menu(1, res.getString(R.string.menu_sale_order),  "Order", R.mipmap.ic_orders),
            Menu(2, res.getString(R.string.menu_ps_order),  "PSOrder",   R.drawable.ic_ps_order),
            Menu(3, res.getString(R.string.menu_sale), "Invoice", R.drawable.ic_sale),
            Menu(4, res.getString(R.string.menu_sale_return), "SaleReturn",  R.drawable.ic_sale_return),
            Menu(5, res.getString(R.string.menu_transfer), "Transfer",  R.drawable.ic_transfer),
            Menu(6, res.getString(R.string.menu_delivery), "Delivery",  R.drawable.ic_delivery  ),
            Menu(7, res.getString(R.string.menu_receivable),"Receivable", R.drawable.ic_receivable),
            Menu(8, res.getString(R.string.menu_payable),"Payable", R.drawable.ic_payment),
            Menu(9, res.getString(R.string.menu_survey), "Survey",    R.drawable.ic_survey),
            Menu(10, res.getString(R.string.menu_customers), "Customer",    R.drawable.ic_customers),
            Menu(11, res.getString(R.string.menu_reports),"Reports",    R.mipmap.ic_reports_1),
            Menu(12, res.getString(R.string.menu_notification), "Alart",  R.drawable.alarm),
            Menu(13, res.getString(R.string.menu_settings), "Settings",  R.drawable.ic_settings)
        )
        menuList.postValue(mList)
        return  menuList
    }

    object loadImage{
        @JvmStatic
        @BindingAdapter("android:imageUrl")
        fun loadImage(view: View, iconId: Int){
            val imgView: ImageView = view as ImageView
            imgView.setImageDrawable(ContextCompat.getDrawable(imgView.context, iconId))
        }
    }
}