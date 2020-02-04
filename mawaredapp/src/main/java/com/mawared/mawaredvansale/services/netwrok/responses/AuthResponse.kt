package com.mawared.mawaredvansale.services.netwrok.responses

import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.security.User

data class AuthResponse (
    val isSuccessful : Boolean, // isSuccessful
    val message : String?, //message
    val user : User?
)


data class ListRecsResponse<T> (
    val isSuccessful : Boolean, // isSuccessful
    val message : String?, //message
    val data : List<T>?
)

data class SingleRecResponse<T> (
    val isSuccessful : Boolean, // isSuccessful
    val message : String?, //message
    val data : T?
)
