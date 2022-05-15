package com.mawared.mawaredvansale.data.db.entities.mnt

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.mawared.mawaredvansale.data.db.entities.dms.Document

@Entity
data class Mnts (
    var doc_no: Int?,            // sale number
    var doc_date: String?,// sale date
    var prefix: String?,         // sale prefix
    var refNo: String?,          // sale reference number
    var client_id: Int?,
    var org_id: Int?,
    var vo_Id: Int?,
    var docTypeId: Int?,
    var ref_no: String?,
    var ref_Id: Int?,
    var cust_Id: Int?,        // Customer code
    var prod_Id: Int?,
    var sr_no: String?,
    var war_id: Int?,
    var war_no: String?,
    var war_date: String?,
    var near_point: String?,
    var address: String?,
    var regId: Int?,
    var regMntNo: Int?,
    var contId: Int?,
    var contRefNo: String?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
    ){
    @PrimaryKey(autoGenerate = true)
    var mntId:  Int = 0
    var vo_code: String? = null
    var docTypeCode: String? = null
    var docTypeName: String? = null
    var cust_name: String? = null
    var prod_name: String? = null
    var totalWorkcost: Double? = null
    var totalItemscost: Double? = null
    var totalCost: Double? = null

    @Ignore
    var docMntTrans : MntTrans? = null
    @Ignore
    var SpPartsLines: ArrayList<MntSpareParts> = arrayListOf()
    @Ignore
    var ServLines: ArrayList<MntServ> = arrayListOf()
    @Ignore
    var TechLines: ArrayList<MntTech> = arrayListOf()
    @Ignore
    var DocLines: ArrayList<Document> = arrayListOf()
}