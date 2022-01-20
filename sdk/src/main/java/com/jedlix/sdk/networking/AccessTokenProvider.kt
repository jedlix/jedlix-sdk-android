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

package com.jedlix.sdk.networking

/**
 * Provides an access token to be used when signing in to the api
 */
interface AccessTokenProvider {
    /**
     * Gets the latest version of the token (if any)
     * Ideally, this method should try and refresh the token
     * @return The latest version of the token if it exists or `null`
     */
    suspend fun getAccessToken(): String?

    /**
     * Requests the access token to be refreshed
     * @return The renewed token if it exists or `null`
     */
    suspend fun renewAccessToken(): String?
}

internal object DefaultAccessTokenProvider : AccessTokenProvider {
    override suspend fun getAccessToken(): String? = null
    override suspend fun renewAccessToken(): String? = null
}
