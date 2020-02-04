package com.mawared.mawaredvansale.services.repositories

import android.content.Context
import android.content.res.Resources
import android.util.Log
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
import com.mawared.mawaredvansale.utilities.ApiException
import kotlinx.coroutines.*

class MenuRepository(
    private val api: ApiService,
    private val db: AppDatabase
) : SafeApiRequest() {
    var job: CompletableJob? = null
    fun getByUserId(userId: Int, lang: String): LiveData<List<Menu>> {
        job = Job()
        return object : LiveData<List<Menu>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiRequest { api.menu_getByUser(userId, lang) }
                            withContext(Dispatchers.Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: java.lang.Exception){
                            Log.e("Exception", "Error exception when call menu get by User Id", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }
    var menuList : MutableLiveData<List<Menu>> = MutableLiveData()
     suspend fun getMenu() : LiveData<List<Menu>>{
         return withContext(Dispatchers.IO){
             db.getMenuDao().getAll()
         }
    }

    fun getLocalMenu(ctx: Context): MutableLiveData<List<Menu>>{
        val res = ctx.resources
        //var id = res.getIdentifier("ic_so", null, ctx.packageName)

        val mList = listOf(
            Menu(1, res.getString(R.string.menu_sale_order),  "Order", "ic_so"),
            Menu(2, res.getString(R.string.menu_ps_order),  "PSOrder",   "ic_ps"),
            Menu(3, res.getString(R.string.menu_sale), "Invoice", "ic_sl"),
            Menu(4, res.getString(R.string.menu_sale_return), "SaleReturn",  "ic_sr"),
            Menu(5, res.getString(R.string.menu_transfer), "Transfer",  "ic_trs"),
            Menu(6, res.getString(R.string.menu_delivery), "Delivery",  "ic_delivery"),
            Menu(7, res.getString(R.string.menu_receivable),"Receivable", "ic_receipt"),
            Menu(8, res.getString(R.string.menu_payable),"Payable", "ic_payment"),
            Menu(9, res.getString(R.string.menu_survey), "Survey",    "ic_survey"),
            Menu(10, res.getString(R.string.menu_customers), "Customer",    "ic_customers"),
            Menu(11, res.getString(R.string.menu_reports),"Reports",    "ic_report1"),
            Menu(12, res.getString(R.string.menu_notification), "Alart",  "ic_alart"),
            Menu(13, res.getString(R.string.menu_settings), "Settings",  "ic_settings1")
        )
        menuList.postValue(mList)
        return  menuList
    }

    object loadImage{
        @JvmStatic
        @BindingAdapter("android:imageUrl")
        fun loadImage(view: View, iconName: String){
            val ctx = view.context
            val iconId = ctx.resources.getIdentifier(iconName, "drawable", ctx.packageName)
            if(iconId != 0){
                val imgView: ImageView = view as ImageView
                imgView.setImageDrawable(ContextCompat.getDrawable(imgView.context, iconId))
            }
        }
    }
}