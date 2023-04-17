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

import com.jedlix.sdk.JedlixSDK
import com.jedlix.sdk.networking.Api.Response.Success
import com.jedlix.sdk.networking.endpoint.EndpointBuilder
import com.jedlix.sdk.networking.endpoint.EndpointNode
import java.util.*

/**
 * Class for requesting data from [EndpointNode]
 */
abstract class Api {

    companion object {
        private const val HEADER_API_KEY = "ApiKey"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
        private const val HEADER_CLIENT_VERSION = "Jedlix-ClientVersion"
        private const val HEADER_CORRELATION_ID = "Jedlix-CorrelationId"

        private const val AUTHORIZATION_FORMAT = "Bearer %s"

        internal const val timeoutMillis: Long = 15000
    }

    /**
     * Response types returned by the [Api]
     * @param Result the type of the result expected on [Success]
     */
    sealed class Response<Result> {

        /**
         * A successful [Response] containing [Result]
         * @property result The received [Result]
         */
        data class Success<Result>(val result: Result) : Response<Result>()

        /**
         * A failed [Response]
         */
        sealed class Failure<Result> : Response<Result>()

        /**
         * A [Response.Failure] caused by an expected [com.jedlix.sdk.networking.Error]
         * @property error the [com.jedlix.sdk.networking.Error] causing this failure
         */
        data class Error<Result>(val error: com.jedlix.sdk.networking.Error) : Failure<Result>()

        /**
         * A [Response.Failure] caused by network issues (e.g. timeout or invalid url)
         */
        class NetworkFailure<Result> : Failure<Result>()

        /**
         * A [Response.Failure] caused by unexpected data being received
         */
        class InvalidResult<Result> : Failure<Result>()

        /**
         * A [Response.Failure] caused by [JedlixSDK] not being initialized properly
         */
        class SDKNotInitialized<Result> : Failure<Result>()
    }

    protected abstract val host: String
    protected abstract val basePath: String
    protected abstract val apiKey: String?
    protected abstract val authentication: Authentication

    protected suspend fun headers(): Map<String, String> = mapOf(
        HEADER_API_KEY to apiKey,
        HEADER_AUTHORIZATION to authentication.getAccessToken()
            ?.let { AUTHORIZATION_FORMAT.format(it) },
        HEADER_ACCEPT_LANGUAGE to Locale.getDefault().toLanguageTag(),
        HEADER_CLIENT_VERSION to "1.5.0",
        HEADER_CORRELATION_ID to UUID.randomUUID().toString()
    )
        .mapNotNull { (key, value) -> value?.let { key to it } }
        .toMap()

    /**
     * Requests an [EndpointNode] for a [Response] of type [Result]
     * @param Result the result to be returned by this request
     * @param builder transforms an [EndpointBuilder] info the [EndpointNode] to be requested
     * @return A [Response] from requesting the endpoint
     */
    suspend fun <Result : Any> request(builder: EndpointBuilder.() -> EndpointNode<Result>): Response<Result> {
        return if (host.isEmpty()) {
            JedlixSDK.logError("SDK has not been initialized properly. Make sure you configure an API host")
            Response.SDKNotInitialized()
        } else {
            val endpoint = EndpointBuilder().builder()
            when (val response = request(endpoint)) {
                is Response.Error -> when (response.error) {
                    is Error.Unauthorized -> {
                        // In case of a 401 error try to refresh the token and do the request again
                        if (authentication.getAccessToken().isNullOrEmpty()) {
                            response
                        } else {
                            request(endpoint)
                        }
                    }
                    else -> response
                }
                else -> response
            }
        }
    }

    protected abstract suspend fun <Result : Any> request(endpoint: EndpointNode<Result>): Response<Result>
}
