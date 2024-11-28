package com.example.pet_universe.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val repository: UserRepository): ViewModel() {
    val allUsersLiveData: LiveData<List<User>> = repository.allUsers.asLiveData()

    fun insert(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(user)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    fun delete(key: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(key)
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return withContext(IO) {
            repository.getUserByEmail(email)
        }
    }

    suspend fun getFirstName(firstName: String): User? {
        return withContext(IO) {
            repository.getFirstName(firstName)
        }
    }

    suspend fun getLastName(lastName: String): User? {
        return withContext(IO) {
            repository.getLastName(lastName)
        }
    }

    suspend fun updateProfile(user: User) {
        withContext(IO) {
            repository.updateProfile(user)
        }
    }

    fun updatePassword(user: User) {
        viewModelScope.launch(IO) {
            repository.updatePassword(user)
        }
    }

}

class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
