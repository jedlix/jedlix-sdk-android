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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jedlix.sdk.JedlixSDK
import com.jedlix.sdk.example.authentication.Authentication
import com.jedlix.sdk.model.Vehicle
import com.jedlix.sdk.networking.Api
import com.jedlix.sdk.networking.Error
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VehicleViewModel(private val userIdentifier: String) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val userIdentifier: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VehicleViewModel(
                userIdentifier
            ) as T
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val vehicle = MutableStateFlow<Vehicle?>(null)
    val text = vehicle.map { vehicle ->
        vehicle?.vehicleDetails?.let { "${it.brand} ${it.model}" } ?: "No vehicles found"
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")
    val buttonText = vehicle.map { vehicle ->
        if (vehicle != null) {
            "Remove"
        } else {
            "Connect"
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    private val _didDeauthenticate = MutableSharedFlow<Unit>()
    val didDeauthenticate: SharedFlow<Unit> = _didDeauthenticate
    private val _didStartConnectSession = MutableSharedFlow<String>()
    val didStartConnectSession: SharedFlow<String> = _didStartConnectSession

    private var lastConnectSessionId: String? = null

    init {
        updateVehicle()
    }

    fun buttonPressed() {
        if (vehicle.value != null) {
            removeVehicle()
        } else {
            startConnectSession()
        }
    }

    fun deauthenticate() {
        Authentication.instance.clearCredentials()
        viewModelScope.launch {
            _didDeauthenticate.emit(Unit)
        }
    }

    fun updateVehicle() {
        showLoaderDuring {
            when (val response = JedlixSDK.api.request { Users().User(userIdentifier).Vehicles().Get() }) {
                is Api.Response.Success -> {
                    vehicle.value = response.result.firstOrNull()
                    _errorMessage.value = null
                }
                is Api.Response.Failure -> response.showMessage()
            }
        }
    }

    private fun removeVehicle() {
        showLoaderDuring {
            val vehicleId = vehicle.value?.id ?: return@showLoaderDuring
            when (
                val response = JedlixSDK.api.request {
                    Users().User(userIdentifier).Vehicles().Vehicle(vehicleId).Delete()
                }
            ) {
                is Api.Response.Success -> vehicle.value = null
                is Api.Response.Failure -> response.showMessage()
            }
        }
    }

    private fun startConnectSession() {
        viewModelScope.launch {
            _didStartConnectSession.emit(userIdentifier)
        }
    }

    private fun showLoaderDuring(action: suspend () -> Unit) {
        if (_isLoading.compareAndSet(expect = false, update = true)) {
            viewModelScope.launch {
                try {
                    action()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun Api.Response.Failure<*>.showMessage() {
        _errorMessage.value = when (this) {
            is Api.Response.Error -> {
                when (error) {
                    is Error.Forbidden -> "Forbidden"
                    is Error.Unauthorized -> "Unauthorized"
                    is Error.ApiError -> (error as Error.ApiError).title
                }
            }
            is Api.Response.NetworkFailure -> "Please check your network"
            is Api.Response.InvalidResult -> "Unexpected response"
            is Api.Response.SDKNotInitialized -> "SDK Not initialized properly"
        }
    }
}
