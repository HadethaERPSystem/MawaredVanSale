package com.mawared.mawaredvansale.data.db.dao

import androidx.room.*


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
    suspend fun insert(baseEo: List<T>)

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