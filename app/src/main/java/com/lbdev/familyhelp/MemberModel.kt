package com.lbdev.familyhelp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MemberModel(
    @PrimaryKey
    val name:String,
    val address:String,
    val battery:String,
    val distance:String,
    val network: String,
    val userImage: String
)
