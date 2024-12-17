package com.example.trackingyou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class UserListViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showAddUserDialog = MutableStateFlow(false)
    val showAddUserDialog: StateFlow<Boolean> = _showAddUserDialog.asStateFlow()

    private val _userToEdit = MutableStateFlow<User?>(null)
    val userToEdit: StateFlow<User?> = _userToEdit.asStateFlow()

    private val _userToDelete = MutableStateFlow<User?>(null)
    val userToDelete: StateFlow<User?> = _userToDelete.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        viewModelScope.launch {
            FirebaseService.fetchUsersFirestoreRealtime(
                onSuccess = { fetchedUsers ->
                    val sortedUsers = fetchedUsers.sortedBy { it.nombre.lowercase(Locale.getDefault()) }
                    _users.value = sortedUsers
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun showAddUserDialog() {
        _showAddUserDialog.value = true
    }

    fun dismissAddUserDialog() {
        _showAddUserDialog.value = false
    }

    fun addUser(newUser: User) {
        viewModelScope.launch {
            FirebaseService.addUserFirestore(
                user = newUser,
                onSuccess = {
                    _showAddUserDialog.value = false
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                    _showAddUserDialog.value = false
                }
            )
        }
    }

    fun editUser(user: User) {
        _userToEdit.value = user
    }

    fun dismissEditUser() {
        _userToEdit.value = null
    }

    fun updateUser(editedUser: User) {
        viewModelScope.launch {
            FirebaseService.updateUserFirestore(
                user = editedUser,
                onSuccess = { _userToEdit.value = null },
                onFailure = { error ->
                    _errorMessage.value = error.message
                    _userToEdit.value = null
                }
            )
        }
    }

    fun deleteUser(user: User) {
        _userToDelete.value = user
    }

    fun dismissDeleteUser() {
        _userToDelete.value = null
    }

    fun confirmDeleteUser(user: User) {
        viewModelScope.launch {
            FirebaseService.deleteUser(
                userId = user.id,
                onComplete = { success ->
                    if (success) { } else {
                        _errorMessage.value = "Error al eliminar el usuario."
                    }
                    _userToDelete.value = null
                }
            )
        }
    }

    fun onSearchTextChange(newText: String) {
        _searchText.value = newText
    }

    fun addRecord(user: User, newRecord: Record) {
        viewModelScope.launch {
            FirebaseService.addRecordToUser(
                userId = user.id,
                record = newRecord,
                onSuccess = {},
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )
        }
    }
}
