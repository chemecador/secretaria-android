package com.chemecador.secretaria.ui.viewmodel.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.data.services.AuthService
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository
) : ViewModel() {

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _authState = MutableLiveData<Resource<FirebaseUser>>()
    val authState: LiveData<Resource<FirebaseUser>> get() = _authState

    private var _signupError = MutableStateFlow<String?>(null)
    val signupError: StateFlow<String?> = _signupError

    fun clearErrors() {
        _loginError.value = null
        _signupError.value = null
    }

    fun signup(user: String, password: String, onSignupSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = withContext(Dispatchers.IO) {
                authService.signup(user, password)
            }

            if (result.isSuccess) {
                onSignupSuccess()
                _signupError.value = null
            } else {
                _signupError.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }


    fun login(user: String, password: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = withContext(Dispatchers.IO) {
                authService.login(user, password)
            }

            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    onLoginSuccess()
                    _loginError.emit(null)
                }
            } else {
                _loginError.emit(result.exceptionOrNull()?.message)
            }

            _isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = withContext(Dispatchers.IO) {
                authService.signInWithGoogle(idToken)
            }

            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    onLoginSuccess()
                    _loginError.emit(null)
                }
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Error"
                _loginError.emit(errorMessage)
            }

            _isLoading.value = false
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val user = userRepository.signInAnonymously()
            if (user != null) {
                _authState.value = Resource.Success(user)
            } else {
                _authState.value = Resource.Error("Error")
            }
        }
    }
}