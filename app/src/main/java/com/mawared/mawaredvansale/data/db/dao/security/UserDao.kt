package com.mawared.mawaredvansale.data.db.dao.security

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.security.User

@Dao
interface UserDao : BaseDao<User> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM User")
    fun getAll(): LiveData<List<User>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM User WHERE id = :id")
    fun getUserById(id: Int): LiveData<User>

    @Query("SELECT * FROM User WHERE user_name = :userName AND password = :password")
    fun userLogin(userName: String, password: String) : LiveData<User>

    @Query("SELECT * FROM user WHERE id = 1")
    fun getUser(): LiveData<User>
}