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

/**
 * An observer that is notified when a [com.jedlix.sdk.model.ConnectSession] has been created or finished
 * It is recommended to store newly created sessions so they can be restored using [ConnectSessionManager.restoreConnectSession].
 * Since some connections can deadlock if they are not finished or cancelled, starting a new session may not always work.
 * It is recommended to store any result remotely
 */
interface ConnectSessionObserver {

    /**
     * Notifies the observer that a new [com.jedlix.sdk.model.ConnectSession] has been created
     * @param userIdentifier The user identifier of the user for whom the connect session was created
     * @param connectSessionIdentifier The [com.jedlix.sdk.model.ConnectSession.id] of the connect session that was created
     */
    fun onConnectSessionCreated(userIdentifier: String, connectSessionIdentifier: String)

    /**
     * Notifies the observer that a new [com.jedlix.sdk.model.ConnectSession] has been finished
     * @param userIdentifier The user identifier of the user for whom the connect session was finished
     * @param connectSessionIdentifier The [com.jedlix.sdk.model.ConnectSession.id] of the connect session that was finished
     */
    fun onConnectSessionFinished(userIdentifier: String, connectSessionIdentifier: String)
}
