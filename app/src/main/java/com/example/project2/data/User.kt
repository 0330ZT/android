package com.example.project2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val username: String,
    val password: String,
    val isAdmin: Boolean = false,
    val avatar: String = "", // 头像路径或URL
    val nickname: String = "" // 昵称
) 