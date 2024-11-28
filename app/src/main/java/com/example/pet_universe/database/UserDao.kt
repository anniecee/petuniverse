package com.example.pet_universe.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM user_table")
    fun getAll(): Flow<List<User>>

    @Query("DELETE FROM user_table")
    suspend fun deleteAll()

    @Query("DELETE FROM user_table WHERE id = :key")
    suspend fun delete(key: Long)

    @Query("SELECT * FROM user_table WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user_table WHERE first_name = :firstName")
    suspend fun getFirstName(firstName: String): User?

    @Query("SELECT * FROM user_table WHERE last_name = :lastName")
    suspend fun getLastName(lastName: String): User?

    @Query("UPDATE user_table SET first_name = :firstName, last_name = :lastName, password =:password WHERE email = :email")
    suspend fun update(firstName: String, lastName: String, email: String, password: String)
}
