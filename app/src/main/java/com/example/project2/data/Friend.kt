package com.example.project2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String, // 当前用户名
    val friendUsername: String, // 好友用户名
    val friendNickname: String = "", // 好友昵称
    val friendAvatar: String = "" // 好友头像
) 