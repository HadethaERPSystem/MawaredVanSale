package com.mawared.mawaredvansale.services.netwrok.responses

import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.security.User

data class AuthResponse (
    val isSuccessful : Boolean, // isSuccessful
    val message : String?, //message
    val user : User?
)


data class ResponseList<T> (
    val isSuccessful : Boolean, // isSuccessful
    val message : String?, //message
    val data : List<T>?,
    val totalPages: Int
)

data class ResponseSingle<T> (
    val isSuccessful : Boolean, // isSuccessful
    val message : String?, //message
    val data : T?
)
