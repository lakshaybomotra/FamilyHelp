package com.lbdev.familyhelp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveLiveStatus(live: UserModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUserName(name: UserModel)

    @Query(value = "Select * from UserModel")
    fun getLiveStatus() : List<UserModel>
}