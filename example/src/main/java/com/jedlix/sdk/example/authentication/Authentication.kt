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

package com.jedlix.sdk.example.authentication

import android.content.Context
import com.jedlix.sdk.networking.AccessTokenProvider
import kotlinx.coroutines.CoroutineScope

sealed interface Authentication : AccessTokenProvider {

    companion object {
        // In a real application you shouldn't use a singleton but dependency injection or something similar
        lateinit var current: Authentication

        fun enableDefault(context: Context) {
            current = DefaultAuthentication(context)
        }

        fun enableAuth0(
            clientId: String,
            domain: String,
            audience: String,
            userIdentifierKey: String,
            coroutineScope: CoroutineScope,
            context: Context
        ) {
            current = Auth0Authentication(clientId, domain, audience, userIdentifierKey, coroutineScope, context)
        }
    }

    sealed class SignInResponse {
        data class Success(val userIdentifier: String) : SignInResponse()
        data class Failed(val error: String) : SignInResponse()
    }

    val isSignedIn: Boolean
    suspend fun getCredentials(): SignInResponse
    fun clearCredentials()
}
