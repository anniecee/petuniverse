package com.example.pet_universe.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {
    val allUsers: Flow<List<User>> = userDao.getAll()

    fun insert(user: User) {
        CoroutineScope(IO).launch {
            userDao.insert(user)
        }
    }

    fun deleteAll() {
        CoroutineScope(IO).launch {
            userDao.deleteAll()
        }
    }

    fun delete(key: Long) {
        CoroutineScope(IO).launch {
            userDao.delete(key)
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return withContext(IO) {
            userDao.getUserByEmail(email)
        }
    }

    suspend fun getFirstName(firstName: String): User? {
        return withContext(IO) {
            userDao.getFirstName(firstName)
        }
    }

    suspend fun getLastName(lastName: String): User? {
        return withContext(IO) {
            userDao.getLastName(lastName)
        }
    }

}
