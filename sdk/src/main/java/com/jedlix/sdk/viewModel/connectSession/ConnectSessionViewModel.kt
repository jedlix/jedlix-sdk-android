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

package com.jedlix.sdk.viewModel.connectSession

import android.webkit.CookieManager
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jedlix.sdk.JedlixSDK
import com.jedlix.sdk.connectSession.ConnectSessionResult
import com.jedlix.sdk.connectSession.ConnectSessionType
import com.jedlix.sdk.model.Alert
import com.jedlix.sdk.model.ConnectSessionDescriptor
import com.jedlix.sdk.networking.Api
import com.jedlix.sdk.networking.Error
import java.net.MalformedURLException
import java.net.URL
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ConnectSessionViewModel(
    arguments: ConnectSessionArguments
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val arguments: ConnectSessionArguments
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConnectSessionViewModel(
                arguments
            ) as T
        }
    }

    private val _session = MutableStateFlow<ConnectSessionDescriptor?>(null)
    internal val session = _session.asStateFlow()

    val isActivityIndicatorVisible =
        _session.map { it == null }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    val webViewUrl: StateFlow<URL?> = _session.map { session ->
        session?.let {
            withContext(Dispatchers.IO) {
                try {
                    URL(it.startUrl)
                } catch (e: MalformedURLException) {
                    null
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _alert = MutableSharedFlow<Alert>()
    val alert: SharedFlow<Alert> = _alert

    private val _onFinished = MutableSharedFlow<ConnectSessionResult>()
    val onFinished: SharedFlow<ConnectSessionResult> = _onFinished

    private val userId = arguments.userId

    init {
        when (arguments) {
            is ConnectSessionArguments.Create -> createConnectSession(arguments.connectSessionType)
            is ConnectSessionArguments.Resume -> resumeConnectSession(arguments.connectSessionId)
            is ConnectSessionArguments.EnergySupplier -> createUtilityConnectSession(
                userId = userId,
                chargingLocationId = arguments.chargingLocationId
            )
        }

        viewModelScope.launch {
            _session.filterNotNull().filter { it.isFinished }.collect {
                _onFinished.emit(ConnectSessionResult.Finished(it.id))
            }
        }

        viewModelScope.launch {
            webViewUrl.filter { it == null }.collect {
                forceCloseSdkWithCurrentState()
            }
        }
    }

    fun toOverrideUrlTypes(url: String): OverrideUrlTypes = OverrideUrlTypes.toUrlType(url, _session.value)

    fun consumeSession(session: ConnectSessionDescriptor, view: WebView, url: String, cookieManager: CookieManager) {
        val redirectUrl = session.redirectUrl
        if (redirectUrl != null && url.startsWith(redirectUrl)) {
            viewModelScope.launch {
                session.redirectInfo?.let {
                    val info = it.connectSessionInfo(view, cookieManager, url)
                    postConnectSessionInfo(session, info)
                } ?: getConnectSession(session.id)
            }
        }
    }

    private fun createConnectSession(type: ConnectSessionType) {
        viewModelScope.launch {
            when (
                val response = JedlixSDK.api.request {
                    Users().User(userId).run {
                        when (type) {
                            is ConnectSessionType.Vehicle -> Vehicles().StartConnectSession()
                            is ConnectSessionType.SelectedVehicle -> Vehicles().Vehicle(type.vehicleId)
                                .StartConnectSession()
                            is ConnectSessionType.Charger -> ChargingLocations().ChargingLocation(
                                type.chargingLocationId
                            ).Chargers().StartConnectSession()

                            is ConnectSessionType.EnergySupplier -> ChargingLocations().ChargingLocation(
                                type.chargingLocationId
                            ).EnergySuppliers().StartConnectSession()
                        }
                    }
                }
            ) {
                is Api.Response.Success -> {
                    _session.value = response.result
                }
                is Api.Response.Failure -> handleFailure(
                    response
                ) {
                    createConnectSession(type)
                }
            }
        }
    }

    private fun createUtilityConnectSession(userId: String, chargingLocationId: String) {
        viewModelScope.launch {
            when (
                val response = JedlixSDK.api.request {
                    Users()
                        .User(userId)
                        .ChargingLocations()
                        .ChargingLocation(chargingLocationId)
                        .EnergySuppliers()
                        .StartConnectSession()
                }
            ) {
                is Api.Response.Success -> {
                    _session.value = response.result
                }
                is Api.Response.Failure -> handleFailure(
                    response
                ) {
                    createUtilityConnectSession(
                        userId = userId,
                        chargingLocationId = chargingLocationId
                    )
                }
            }
        }
    }

    internal fun resumeConnectSession(sessionId: String) {
        viewModelScope.launch {
            when (
                val response = JedlixSDK.api.request {
                    Users().User(userId).ConnectSessions().Session(sessionId).Get()
                }
            ) {
                is Api.Response.Success -> _session.value = response.result
                is Api.Response.Failure -> handleFailure(response) {
                    resumeConnectSession(sessionId)
                }
            }
        }
    }

    internal suspend fun getConnectSession(sessionId: String) {
        when (
            val response = JedlixSDK.api.request {
                Users().User(userId).ConnectSessions().Session(sessionId).Get()
            }
        ) {
            is Api.Response.Success -> _session.value = response.result
            is Api.Response.Failure -> handleFailure(response) {
                viewModelScope.launch {
                    getConnectSession(sessionId)
                }
            }
        }
    }

    /**
     * It will emit [ConnectSessionResult.InProgress] if the session was started. Otherwise [ConnectSessionResult.NotStarted].
     *
     * This function will finish the sdk activity.
     */
    internal fun forceCloseSdkWithCurrentState() {
        viewModelScope.launch {
            _onFinished.emit(
                value = _session.value?.id?.let {
                    ConnectSessionResult.InProgress(sessionId = it)
                } ?: ConnectSessionResult.NotStarted
            )
        }
    }

    private fun postConnectSessionInfo(connectSession: ConnectSessionDescriptor, info: ConnectSessionDescriptor.Info) {
        viewModelScope.launch {
            when (
                val response = JedlixSDK.api.request {
                    Users().User(userId).ConnectSessions().Session(connectSession.id).Info(info)
                }
            ) {
                is Api.Response.Success -> _session.value = response.result
                is Api.Response.Failure -> handleFailure(response) {
                    postConnectSessionInfo(connectSession, info)
                }
            }
        }
    }

    private fun <Response> handleFailure(
        failure: Api.Response.Failure<Response>,
        onRetry: () -> Unit
    ) {
        when (failure) {
            is Api.Response.Error -> when (val error = failure.error) {
                is Error.Unauthorized -> {
                    JedlixSDK.logError("Failed to authorize")
                    null
                }
                is Error.Forbidden -> {
                    JedlixSDK.logError("Failed to authorize")
                    null
                }
                is Error.ApiError -> {
                    (error.title ?: "Unknown error") to (error.detail ?: "")
                }
            }
            is Api.Response.NetworkFailure -> {
                "Network error" to "Try again later"
            }
            is Api.Response.InvalidResult -> {
                JedlixSDK.logError("Invalid connect session result")
                null
            }
            is Api.Response.SDKNotInitialized -> {
                null
            }
        }?.let { (title, message) ->
            showAlert(title, message, onRetry)
        } ?: run {
            forceCloseSdkWithCurrentState()
        }
    }

    private fun showAlert(title: String, message: String, onRetry: () -> Unit) {
        viewModelScope.launch {
            _alert.emit(
                Alert(
                    title,
                    message,
                    Alert.Button("Retry", onRetry),
                    Alert.Button("Cancel") {
                        forceCloseSdkWithCurrentState()
                    }
                )
            )
        }
    }

    private suspend fun ConnectSessionDescriptor.RedirectInfo.connectSessionInfo(
        view: WebView,
        cookieManager: CookieManager,
        url: String
    ): ConnectSessionDescriptor.Info {
        val body = if (includeBody) {
            val completableBody = CompletableDeferred<String?>()
            view.evaluateJavascript("document.documentElement.outerHTML.toString()") {
                completableBody.complete(it)
            }
            completableBody.await()
        } else {
            null
        }
        val cookiesForUrl = cookieManager.getCookie(url) ?: ""
        val cookieMap = cookiesForUrl.split(";").associateBy(
            { it.substringBefore("=").trim() },
            { it.substringAfter("=").trim() }
        ).filterKeys { includeCookies.contains(it) }

        return ConnectSessionDescriptor.Info(
            body,
            cookieMap,
            if (includeRedirectUrl) url else null
        )
    }
}
