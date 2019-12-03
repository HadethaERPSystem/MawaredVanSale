package com.mawared.mawaredvansale.data.db.entities.srv

import androidx.room.PrimaryKey

class Question( var qn_Id: Int = 0,
                var qn_name: String? = null,
                var qn_input_type: String? = null,
                var qn_option: List<String>?,
                var survey_Id: Int? = null){
    var qn_answer : String? = null
}