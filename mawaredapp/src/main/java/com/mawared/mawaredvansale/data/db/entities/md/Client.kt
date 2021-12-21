package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Client(
    @ColumnInfo(name = "Name")
    var name : String?
){
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "CId")
    var cId: Int = 0
}