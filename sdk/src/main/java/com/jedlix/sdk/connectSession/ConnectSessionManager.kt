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

package com.jedlix.sdk.connectSession

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import com.jedlix.sdk.activity.connectSession.ConnectSessionActivity
import com.jedlix.sdk.model.ConnectSessionDescriptor
import com.jedlix.sdk.viewModel.connectSession.ConnectSessionArguments

/**
 * Handles the management of a [com.jedlix.sdk.model.ConnectSessionDescriptor]
 */
interface ConnectSessionManager {

    /**
     * Starts the process of a new [com.jedlix.sdk.model.ConnectSessionDescriptor]
     * @param userIdentifier The user identifier of the user for whom to start the session
     * @param connectSessionType The [ConnectSessionManager.ConnectSessionType] for creating the new session
     */
    fun startConnectSession(
        userIdentifier: String,
        connectSessionType: ConnectSessionType
    )

    /**
     * Starts the process of a new [com.jedlix.sdk.model.ConnectSessionDescriptor] for connecting a [com.jedlix.sdk.model.Vehicle]
     * @param userIdentifier The user identifier of the user for whom to start the session
     */
    fun startVehicleConnectSession(
        userIdentifier: String
    ) = startConnectSession(userIdentifier, ConnectSessionType.Vehicle)

    /**
     * Starts the process of a new [com.jedlix.sdk.model.ConnectSessionDescriptor] for connecting a selected [com.jedlix.sdk.model.Vehicle]
     * @param userIdentifier The user identifier of the user for whom to start the session
     * @param vehicleIdentifier The vehicle identifier of the vehicle to connect
     */
    fun startSelectedVehicleConnectSession(
        userIdentifier: String,
        vehicleIdentifier: String
    ) = startConnectSession(userIdentifier, ConnectSessionType.SelectedVehicle(vehicleIdentifier))

    /**
     * Starts the process of a new [com.jedlix.sdk.model.ConnectSessionDescriptor] for connecting a [com.jedlix.sdk.model.Charger]
     * @param userIdentifier The user identifier of the user for whom to start the session
     * @param chargingLocationId The [com.jedlix.sdk.model.ChargingLocation.id] of the location for which to start the session
     */
    fun startChargerConnectSession(
        userIdentifier: String,
        chargingLocationId: String
    ) = startConnectSession(userIdentifier, ConnectSessionType.Charger(chargingLocationId))

    /**
     * Resumes a given [com.jedlix.sdk.model.ConnectSessionDescriptor]
     * @param userIdentifier The user identifier of the user for whom to start the session
     * @param connectSessionIdentifier The [ConnectSessionDescriptor.id] of the connect session to resume
     */
    fun resumeConnectSession(userIdentifier: String, connectSessionIdentifier: String)
}

internal class ConnectSessionManagerImpl(
    private val launcher: ActivityResultLauncher<ConnectSessionArguments>
) : ConnectSessionManager {

    override fun startConnectSession(userIdentifier: String, connectSessionType: ConnectSessionType) {
        launcher.launch(ConnectSessionArguments.Create(userIdentifier, connectSessionType))
    }

    override fun resumeConnectSession(userIdentifier: String, connectSessionIdentifier: String) {
        launcher.launch(ConnectSessionArguments.Resume(userIdentifier, connectSessionIdentifier))
    }
}

/**
 * Generates a [ConnectSessionManager] for a given [ComponentActivity].
 * Registering this requires a [callback] that will be called when a [com.jedlix.sdk.model.ConnectSessionDescriptor] has been started/resumed and then either finished or cancelled
 * @param callback This [ActivityResultCallback] will be called after the [ConnectSessionManager] has been started/resumed and then finished or cancelled. Returns a [ConnectSessionResult].
 */
fun ComponentActivity.registerConnectSessionManager(callback: ActivityResultCallback<ConnectSessionResult>): ConnectSessionManager {
    return ConnectSessionManagerImpl(
        registerForActivityResult(ConnectSessionActivity.Contract(), callback)
    )
}
