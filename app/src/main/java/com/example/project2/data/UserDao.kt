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

    // 游戏规则相关
    @Query("SELECT * FROM game_rules WHERE id = 1")
    suspend fun getGameRules(): GameRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameRule(gameRule: GameRule)

    // 游戏记录相关
    @Insert
    suspend fun insertGameRecord(gameRecord: GameRecord): Long

    @Update
    suspend fun updateGameRecord(gameRecord: GameRecord)

    @Query("SELECT * FROM game_records WHERE id = :gameId")
    suspend fun getGameRecord(gameId: Int): GameRecord?

    @Query("SELECT * FROM game_records WHERE player1 = :username OR player2 = :username ORDER BY startTime DESC")
    suspend fun getUserGameRecords(username: String): List<GameRecord>

    // 游戏步骤相关
    @Insert
    suspend fun insertGameMove(gameMove: GameMove)

    @Query("SELECT * FROM game_moves WHERE gameId = :gameId ORDER BY moveNumber")
    suspend fun getGameMoves(gameId: Int): List<GameMove>

    @Query("DELETE FROM game_moves WHERE gameId = :gameId AND moveNumber > :moveNumber")
    suspend fun deleteMovesAfter(gameId: Int, moveNumber: Int)

    @Query("SELECT * FROM game_moves WHERE gameId = :gameId ORDER BY moveNumber DESC LIMIT 1")
    suspend fun getLastMove(gameId: Int): GameMove?
} 