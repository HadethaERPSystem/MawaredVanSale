package com.mawared.mawaredvansale.controller.auth

import com.mawared.mawaredvansale.data.db.entities.security.User

interface IAuthListener {
    // when operation is stared we need to display prograss bar for user
    fun onStarted()
    //this function is called when operation is successful
    fun onSuccess(user: User)
    //when the operation fail we need to know why this operation is fail
    fun onFailure(message: String)
}