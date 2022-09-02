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
import com.jedlix.sdk.connectSession.ConnectSessionType
import com.jedlix.sdk.example.ExampleApplication
import com.jedlix.sdk.example.model.Button
import com.jedlix.sdk.model.*
import com.jedlix.sdk.networking.Api
import com.jedlix.sdk.networking.Error
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConnectionsViewModel(private val userIdentifier: String) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val userIdentifier: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConnectionsViewModel(
                userIdentifier
            ) as T
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    private val vehicleSessions = MutableStateFlow<List<VehicleConnectSession>>(emptyList())
    val vehicleConnectSessionViewModels = combine(vehicles, vehicleSessions) { vehicles, vehicleSessions ->
        vehicles.map { vehicle ->
            VehicleViewModel(vehicle, vehicleSessions.firstOrNull { it.vehicleId == vehicle.id  }, this::resumeConnectSession, this::removeVehicle)
        }.ifEmpty {
            val connectSessionWithoutVehicle = vehicleSessions.firstOrNull { it.vehicleId.isNullOrEmpty() }
            listOf(
                object : ConnectSessionViewModel {
                    override val title: String = if (connectSessionWithoutVehicle != null) "Unfinished connect session" else "No vehicles found"
                    override val buttons: List<Button> = listOf(
                        connectSessionWithoutVehicle?.let { Button("Resume") { resumeConnectSession(it) } } ?: Button("Connect") { startVehicleConnectSession() }
                    )
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val chargingLocations = MutableStateFlow(emptyList<ChargingLocation>())
    private val chargers = MutableStateFlow<List<Charger>>(emptyList())
    private val chargerSessions = MutableStateFlow<List<ChargerConnectSession>>(emptyList())
    val chargerConnectSessionViewModels = combine(chargers, chargerSessions, chargingLocations) { chargers, chargerSessions, chargingLocations ->
        val chargersPerLocation = chargers.groupBy { it.chargingLocationId }
        val sessionsPerLocation = chargerSessions.groupBy { it.chargingLocationId }
        chargingLocations.map { chargingLocation ->
            ChargingLocationViewModel(
                chargingLocation,
                chargersPerLocation[chargingLocation.id] ?: emptyList(),
                sessionsPerLocation[chargingLocation.id] ?: emptyList(),
                this::startChargerConnectSession,
                this::resumeConnectSession,
                this::removeCharger
            )
        }.ifEmpty {
            listOf(
                object : GroupedConnectSessionViewModel {
                    override val title: String = "No charging locations found"
                    override val connectSessionViewModels: List<ConnectSessionViewModel> = emptyList()
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _didDeauthenticate = MutableSharedFlow<Unit>()
    val didDeauthenticate: SharedFlow<Unit> = _didDeauthenticate
    private val _didStartConnectSession = MutableSharedFlow<Pair<String, ConnectSessionType>>()
    val didStartConnectSession: SharedFlow<Pair<String, ConnectSessionType>> = _didStartConnectSession
    private val _didResumeConnectSession = MutableSharedFlow<Pair<String, String>>()
    val didResumeConnectSession: SharedFlow<Pair<String, String>> = _didResumeConnectSession

    init {
        reloadData()
    }

    fun deauthenticate() {
        ExampleApplication.authentication.deauthenticate()
        viewModelScope.launch {
            _didDeauthenticate.emit(Unit)
        }
    }

    fun reloadData() {
        showLoaderDuring {
            val vehicleResponse = viewModelScope.async {
                when (val response = JedlixSDK.api.request { Users().User(userIdentifier).Vehicles().Get() }) {
                    is Api.Response.Success -> {
                        vehicles.value = response.result
                        null
                    }
                    is Api.Response.Failure -> response.message
                }
            }

            val chargingLocationsResponse = viewModelScope.async {
                when (val response = JedlixSDK.api.request { Users().User(userIdentifier).ChargingLocations().Get() }) {
                    is Api.Response.Success -> {
                        chargingLocations.value = response.result
                        null
                    }
                    is Api.Response.Failure -> response.message
                }
            }

            val chargersResponse = viewModelScope.async {
                when (val response = JedlixSDK.api.request { Users().User(userIdentifier).Chargers().Get() }) {
                    is Api.Response.Success -> {
                        chargers.value = response.result
                        null
                    }
                    is Api.Response.Failure -> {
                        response.message
                    }
                }
            }

            val vehicleSessionsResponse = viewModelScope.async {
                when (val response = JedlixSDK.api.request { Users().User(userIdentifier).ConnectSessions().GetVehicleConnectSessions() }) {
                    is Api.Response.Success -> {
                        vehicleSessions.value = response.result
                        null
                    }
                    is Api.Response.Failure -> response.message
                }
            }

            val chargerSessionsResponse = viewModelScope.async {
                when (val response = JedlixSDK.api.request { Users().User(userIdentifier).ConnectSessions().GetChargerConnectSessions() }) {
                    is Api.Response.Success -> {
                        chargerSessions.value = response.result
                        null
                    }
                    is Api.Response.Failure -> response.message
                }
            }

            val messages = listOf(vehicleResponse, chargingLocationsResponse, chargersResponse, vehicleSessionsResponse, chargerSessionsResponse).awaitAll()
            _errorMessage.value = messages.filterNotNull().joinToString(", ")
        }
    }

    private fun removeVehicle(vehicle: Vehicle) {
        showLoaderDuring {
            when (
                val response = JedlixSDK.api.request {
                    Users().User(userIdentifier).Vehicles().Vehicle(vehicle.id).Delete()
                }
            ) {
                is Api.Response.Success -> vehicles.value = vehicles.value.toMutableList().apply { removeAll { it.id == vehicle.id } }
                is Api.Response.Failure -> _errorMessage.value = response.message
            }
        }
    }

    private fun removeCharger(charger: Charger) {
        showLoaderDuring {
            val chargerId = charger.id
            val chargingLocationId = charger.chargingLocationId
            when (
                val response = JedlixSDK.api.request {
                    Users().User(userIdentifier).ChargingLocations().ChargingLocation(chargingLocationId).Chargers().Charger(chargerId).Delete()
                }
            ) {
                is Api.Response.Success -> chargers.value = chargers.value.toMutableList().apply { removeAll { it.id == chargerId } }
                is Api.Response.Failure -> _errorMessage.value = response.message
            }
        }
    }

    private fun startVehicleConnectSession() {
        viewModelScope.launch {
            _didStartConnectSession.emit(userIdentifier to ConnectSessionType.Vehicle)
        }
    }

    private fun startChargerConnectSession(chargingLocation: ChargingLocation) {
        viewModelScope.launch {
            _didStartConnectSession.emit(userIdentifier to ConnectSessionType.Charger(chargingLocation.id))
        }
    }

    private fun resumeConnectSession(connectSession: ConnectSessionDescriptor) {
        viewModelScope.launch {
            _didResumeConnectSession.emit(userIdentifier to connectSession.id)
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

    private val Api.Response.Failure<*>.message: String? get() = when (this) {
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
