package com.lbdev.familyhelp

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserModel(
    @PrimaryKey
    var live: String = "liveStatus",

    @ColumnInfo(name ="LiveStatus")
    var liveStatus:  Boolean = false
)