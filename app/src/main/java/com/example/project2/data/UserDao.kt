package com.example.project2.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun getUser(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM friends WHERE username = :username")
    suspend fun getFriends(username: String): List<Friend>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend)

    @Delete
    suspend fun deleteFriend(friend: Friend)

    @Query("SELECT * FROM friends WHERE username = :username AND friendUsername = :friendUsername")
    suspend fun getFriend(username: String, friendUsername: String): Friend?
} 