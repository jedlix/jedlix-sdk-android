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

sealed interface Authentication : com.jedlix.sdk.networking.Authentication {
    val isAuthenticated: Boolean
    suspend fun getUserIdentifier(): AuthenticationResponse
    fun deauthenticate()
}

sealed class AuthenticationResponse {
    data class Success(val userIdentifier: String) : AuthenticationResponse()
    data class Failure(val error: String) : AuthenticationResponse()
}
