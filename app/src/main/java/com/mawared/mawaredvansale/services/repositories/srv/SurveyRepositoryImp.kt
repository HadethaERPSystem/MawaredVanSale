package com.mawared.mawaredvansale.services.repositories.srv

import android.util.Log
import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.srv.Question
import com.mawared.mawaredvansale.data.db.entities.srv.Survey_Detail
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.utilities.ApiException
import kotlinx.coroutines.*
import org.threeten.bp.LocalDate


class SurveyRepositoryImp(private val api: ApiService): SafeApiRequest() {
    var job: CompletableJob? = null


    fun getSurvey(): LiveData<List<Question>> {
        job = Job()
        return object : LiveData<List<Question>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {

                            withContext(Dispatchers.Main) {
                                value = getVirtualData()
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: java.lang.Exception){
                            Log.e("Exception", "Error exception when call getCustomers", e)
                            return@launch
                        }
                    }
                }
            }
        }


    }

    fun getVirtualData(): List<Question>{
        return listOf(
            Question(0, "مكان عرض المنتجات", "radio", listOf("Good", "Medium", "Not Good"), 0),
            Question(1, "عدد الانواع المنافسة", "text", null, 0),
            Question(2, "سبب المنافسة", "radio", listOf("Price", "Offer", "Debit", "Quality", "Taste","Not Available"), 1),
            Question(3, "اسماء الماركات المنافسة", "text", null, 1),
            Question(4, "هل الزبون  راضي عن المندوب", "text", null, 1),
            Question(5, "هل الزبون راضي عن العروض", "text", null,0),
            Question(6, "هل هناك مقترحات", "area", null,0),
            Question(7, "هل هناك شكوى", "text", null,1),
            Question(8, "هل تصلك العروض", "radio", listOf("Yes","No"), 0),
            Question(9, "هل تصلك التخفيضات", "radio", listOf("Yes","No"), 1),
            Question(10, "هل لديك مواد تالقة", "radio", listOf("Yes","No"), 0),
            Question(11, "هل لديك منتجات منتهية الصلاحية", "radio", listOf("Yes","No"), 0),
            Question(12, "اخر زيارة للمندوب", "text", null, 1),
            Question(13, "ما رايك بالاسعار", "text", null, 1),
            Question(14, "ما رايك بالمنتجات", "", null,0),
            Question(15, "افضل منتج", "text", null,0),
            Question(16, "اسوء منتج", "text", null,0),
            Question(17, "منتج جديد", "text", null, 1),
            Question(18, "منتج ترغب بشراءه", "text", null, 1),
            Question(19, "افضل مندوب", "text", null,0),
            Question(20, "اسوء مندوب", "text", null,1)
        )
    }
}

