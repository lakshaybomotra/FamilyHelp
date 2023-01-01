package com.lbdev.familyhelp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memberModel: MemberModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memberModelList: List<MemberModel>)

    @Query("SELECT * FROM memberModel")
    fun getAllMembers():LiveData<List<MemberModel>>
}