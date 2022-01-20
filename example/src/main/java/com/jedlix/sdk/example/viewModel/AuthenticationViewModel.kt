/*
 * Copyright 2022 Jedlix B.V. The Netherlands
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.jedlix.sdk.example.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jedlix.sdk.example.authentication.Auth0Authentication
import com.jedlix.sdk.example.authentication.Authentication
import com.jedlix.sdk.example.authentication.DefaultAuthentication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {

    private val authentication: Authentication = Authentication.instance

    private val _didAuthenticate = MutableSharedFlow<String>()
    val didAuthenticate: SharedFlow<String> = _didAuthenticate

    val title = when (authentication) {
        is Auth0Authentication -> "Authenticate a user with Auth0:"
        is DefaultAuthentication -> "Authenticate a user:"
    }

    val email = MutableStateFlow("")
    val isEmailVisible = when (authentication) {
        is Auth0Authentication -> true
        is DefaultAuthentication -> false
    }
    val password = MutableStateFlow("")
    val isPasswordVisible = when (authentication) {
        is Auth0Authentication -> true
        is DefaultAuthentication -> false
    }

    val token = MutableStateFlow("")
    val isTokenVisible = when (authentication) {
        is Auth0Authentication -> false
        is DefaultAuthentication -> true
    }
    val userIdentifier = MutableStateFlow("")
    val isUserIdentifierVisible = when (authentication) {
        is Auth0Authentication -> false
        is DefaultAuthentication -> true
    }

    private val _isLoading = MutableStateFlow(false)
    val buttonEnabled = _isLoading.map { !it }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val buttonText = _isLoading.map { if (it) "Loading..." else "Authenticate" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        if (authentication.isAuthenticated) {
            viewModelScope.launch(Dispatchers.IO) {
                parseSignInResponse(authentication.getCredentials(), false)
            }
        }
    }

    fun authenticate() {
        if (_isLoading.compareAndSet(expect = false, update = true)) {
            _errorMessage.value = null
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val signInResponse = when (authentication) {
                        is Auth0Authentication -> authentication.signIn(email.value, password.value)
                        is DefaultAuthentication -> authentication.setCredentials(token.value, userIdentifier.value)
                    }
                    parseSignInResponse(signInResponse, true)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun parseSignInResponse(authenticationResponse: Authentication.AuthenticationResponse, displayError: Boolean) {
        viewModelScope.launch {
            when (authenticationResponse) {
                is Authentication.AuthenticationResponse.Success -> _didAuthenticate.emit(authenticationResponse.userIdentifier)
                is Authentication.AuthenticationResponse.Failed -> {
                    Log.e("AuthenticationViewModel", "Failed to sign in: ${authenticationResponse.error}")
                    if (displayError) {
                        _errorMessage.value = authenticationResponse.error
                    }
                }
            }
        }
    }
}
