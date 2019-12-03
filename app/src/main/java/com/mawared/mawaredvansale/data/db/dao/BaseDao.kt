package com.mawared.mawaredvansale.data.db.dao

import android.content.ClipData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.mawared.mawaredvansale.data.db.entities.security.User
import androidx.room.OnConflictStrategy
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



@Dao
interface BaseDao<T> {

    /**
     * Insert an object in the database.
     *
     * @param baseEo the object to be inserted.
     */
    @Insert
    suspend fun insert(baseEo: T) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(baseEo: T)

    /**
     * Insert an array of objects in the database.
     *
     * @param baseEo the objects to be inserted.
     */
    @Insert
    suspend fun insert(vararg baseEo: T)

    /**
     * Update an object from the database.
     *
     * @param baseEo the object to be updated
     */
    @Update
    suspend fun update(baseEo: T)

    /**
     * Delete an object from the database
     *
     * @param baseEo the object to be deleted
     */
    @Delete
    suspend fun delete(baseEo: T)
}