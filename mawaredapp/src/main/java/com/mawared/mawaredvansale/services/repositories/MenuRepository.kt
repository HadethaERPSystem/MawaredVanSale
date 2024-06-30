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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.HttpException
import com.bumptech.glide.request.RequestOptions
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.URL_GET_IMAGE
import com.mawared.mawaredvansale.utilities.URL_LOGO
import kotlinx.coroutines.*

class MenuRepository(
    private val api: ApiService,
    private val db: AppDatabase
) : SafeApiRequest() {
    var job: CompletableJob? = null

    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState>
        get() = _networkState

    fun getByUserId(userId: Int, lang: String): LiveData<List<Menu>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Menu>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiRequest { api.menu_getByUser(userId, lang) }
                            withContext(Dispatchers.Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            withContext(Dispatchers.Main){
                                value = null
                                _networkState.postValue(NetworkState.ERROR)
                                job?.complete()
                            }
                        }catch (e: NoConnectivityException){
                            withContext(Dispatchers.Main){
                                value = null
                                _networkState.postValue(NetworkState.ERROR_CONNECTION)
                                job?.complete()
                            }
                            Log.e("Connectivity", "No internet connection", e)
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call menu get by User Id", e)
                            withContext(Dispatchers.Main){
                                value = null
                                _networkState.postValue(NetworkState.ERROR)
                                job?.complete()
                            }
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
            Menu(1, res.getString(R.string.menu_sale_order),  "Order", "ic_so",""),
            Menu(2, res.getString(R.string.menu_ps_order),  "PSOrder",   "ic_ps",""),
            Menu(3, res.getString(R.string.menu_sale), "Invoice", "ic_sl",""),
            Menu(4, res.getString(R.string.menu_sale_return), "SaleReturn",  "ic_sr",""),
            Menu(5, res.getString(R.string.menu_transfer), "Transfer",  "ic_trs",""),
            Menu(6, res.getString(R.string.menu_delivery), "Delivery",  "ic_delivery",""),
            Menu(7, res.getString(R.string.menu_receivable),"Receivable", "ic_receipt",""),
            Menu(8, res.getString(R.string.menu_payable),"Payable", "ic_payment",""),
            Menu(9, res.getString(R.string.menu_survey), "Survey",    "ic_survey",""),
            Menu(10, res.getString(R.string.menu_customers), "Customer",    "ic_customers",""),
            Menu(11, res.getString(R.string.menu_reports),"Reports",    "ic_report1",""),
            Menu(12, res.getString(R.string.menu_notification), "Alart",  "ic_alart",""),
            Menu(13, res.getString(R.string.menu_settings), "Settings",  "ic_settings1","")
        )
        menuList.postValue(mList)
        return  menuList
    }

    fun getReportLocalMenu(ctx: Context): MutableLiveData<List<Menu>>{
        val res = ctx.resources
        //var id = res.getIdentifier("ic_so", null, ctx.packageName)

        val mList = listOf(
            Menu(1, res.getString(R.string.menu_customer_statement),  "CustomerStatement", "ic_cu_statement",""),
            Menu(2, res.getString(R.string.menu_cashbook_statement),  "CashbookStatement", "ic_cashbook",""),
            Menu(3, res.getString(R.string.menu_sales_statement),  "SalesStatement",   "ic_sales",""),
            Menu(4, res.getString(R.string.menu_stock_statement), "StockStatement", "ic_stock","")
        )
        menuList.postValue(mList)
        return  menuList
    }

    object loadImage {
        @JvmStatic
        @BindingAdapter("android:imageUrl")
        fun loadImage(view: View, iconName: String) {
            try {
                val ctx = view.context
                val iconId = ctx.resources.getIdentifier(iconName, "drawable", ctx.packageName)
                if (iconId != 0) {
                    val imgView: ImageView = view as ImageView
                    imgView.setImageDrawable(ContextCompat.getDrawable(imgView.context, iconId))
                }
            }
            catch (e: Exception){}
            catch (e: Resources.NotFoundException){}
            catch (e: HttpException){  }
        }
    }

    object loadUrlLogo{
        @JvmStatic
        @BindingAdapter("android:urlRemoteLogo")
        fun loadImage(view: ImageView, imgeUrl: String){
            try {
                Glide.with(view.context)
                    .load(URL_GET_IMAGE + "/CompanyInfo/" + imgeUrl).apply(RequestOptions().fitCenter())
                    .into(view)
            }
            catch (e: Exception){}
            catch (e: Resources.NotFoundException){}
            catch (e: HttpException){  }
        }
    }
}