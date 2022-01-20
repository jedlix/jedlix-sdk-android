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
import com.jedlix.sdk.activity.connectSession.JedlixConnectSessionActivity
import com.jedlix.sdk.model.ConnectSession
import com.jedlix.sdk.viewModel.connectSession.ConnectSessionArguments

/**
 * Handles the management of a [com.jedlix.sdk.model.ConnectSession]
 */
interface JedlixConnectSessionManager {

    /**
     * Starts the process of a new [com.jedlix.sdk.model.ConnectSession]
     * @param userIdentifier The user identifier of the user for whom to start the session
     * @param settings The [ConnectSession.Settings] for creating the new session
     */
    fun startConnectSession(
        userIdentifier: String,
        settings: ConnectSession.Settings
    )

    /**
     * Starts the process of a new [com.jedlix.sdk.model.ConnectSession] for connecting a [com.jedlix.sdk.model.Vehicle]
     * @param userIdentifier The user identifier of the user for whom to start the session
     */
    fun startVehicleConnectSession(
        userIdentifier: String
    ) = startConnectSession(userIdentifier, ConnectSession.Settings(type = ConnectSession.Settings.Type.Vehicle))

    /**
     * Resumes a given [com.jedlix.sdk.model.ConnectSession]
     * @param userIdentifier The user identifier of the user for whom to start the session
     * @param connectSessionIdentifier The [ConnectSession.id] of the connect session to restore
     */
    fun restoreConnectSession(userIdentifier: String, connectSessionIdentifier: String)
}

internal class JedlixConnectSessionManagerImpl(
    private val launcher: ActivityResultLauncher<ConnectSessionArguments>
) : JedlixConnectSessionManager {

    override fun startConnectSession(userIdentifier: String, settings: ConnectSession.Settings) {
        launcher.launch(ConnectSessionArguments.Create(userIdentifier, settings))
    }

    override fun restoreConnectSession(userIdentifier: String, connectSessionIdentifier: String) {
        launcher.launch(ConnectSessionArguments.Restore(userIdentifier, connectSessionIdentifier))
    }
}

/**
 * Generates a [JedlixConnectSessionManager] for a given [ComponentActivity].
 * Registering this requires a [callback] that will be called when a [com.jedlix.sdk.model.ConnectSession] has been started/resumed and then either finished or cancelled
 * @param callback This [ActivityResultCallback] will be called after the [JedlixConnectSessionManager] has been started/resumed and then finished or cancelled. Returns a [ConnectSessionResult].
 */
fun ComponentActivity.registerForJedlixConnectSession(callback: ActivityResultCallback<ConnectSessionResult>): JedlixConnectSessionManager {
    return JedlixConnectSessionManagerImpl(
        registerForActivityResult(JedlixConnectSessionActivity.Contract(), callback)
    )
}
