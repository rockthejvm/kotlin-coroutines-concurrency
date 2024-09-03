package com.rockthejvm.socialApp


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class UserProfileViewModel(
    private val repo: UserRepository,
    private val coroutineScope: CoroutineScope
) {
    // the state of the UI
    // the current user profile
    private val _profile: MutableStateFlow<UserProfile?> = MutableStateFlow(null) // writable thead-safe variable
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow() // read-only

    // loading state (true/false)
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun loadUserProfile(userId: String) =
        coroutineScope.launch {
            _loading.value = true // assignment is thread-safe
            try {
                _profile.value = repo.fetchProfile(userId)
            } finally {
                _loading.value = false
            }
        }

    fun updateUserProfile(name: String, age: Int) =
        coroutineScope.launch {
            _profile.value?.let { currentProfile -> 
                _loading.value = true
                try {
                    val updatedProfile = currentProfile.copy(name = name, age = age)
                    if (repo.updateProfile(updatedProfile)) {
                        _profile.value = updatedProfile
                    }
                } finally {
                    _loading.value = false
                }
            }
        }
}