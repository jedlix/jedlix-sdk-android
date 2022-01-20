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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.Locale

/**
 * Errors that can be the result of an [Api.Response.Error]
 */
sealed interface Error {
    /**
     * Unauthorized [Error] (status 401)
     */
    object Unauthorized : Error

    /**
     * Forbidden [Error] (status 403)
     */
    object Forbidden : Error

    /**
     * [Error] with body thrown by most requests
     */
    sealed interface ApiError : Error {
        /**
         * The type of the error
         */
        val type: String?

        /**
         * The title of the error
         */
        val title: String?

        /**
         * Http status of the error
         */
        val status: Int?

        /**
         * Detail of the error
         */
        val detail: String?

        /**
         * Instance of the error
         */
        val instance: String?
    }

    /**
     * Default implementation of [ApiError]
     */
    @Serializable
    data class DefaultApiError(
        override val type: String?,
        override val title: String?,
        override val status: Int?,
        override val detail: String?,
        override val instance: String?
    ) : ApiError

    /**
     * Extended implementation of [ApiError]
     * @param Extension the type of data in the [extensions] property
     * @property extensions The [Extension] data of this error
     */
    @Serializable
    data class ExtendedApiError<Extension>(
        override val type: String?,
        override val title: String?,
        override val status: Int?,
        override val detail: String?,
        override val instance: String?,
        val extensions: Extension?
    ) : ApiError
}

/**
 * [Exception] to be thrown by an [Api]
 */
class ApiException(
    internal val code: Int,
    internal val path: String,
    override val message: String = "Non-successful api response for [$path] ($code)",
    internal val responseContentType: String? = null,
    internal val responseContentSubType: String? = null,
    internal val responseBody: String? = null
) : Exception() {

    companion object {
        internal val json = Json {
            isLenient = false
            ignoreUnknownKeys = true
            allowSpecialFloatingPointValues = false
            useArrayPolymorphism = false
        }
    }

    /**
     * Converts this [ApiException] to an [Error.DefaultApiError]
     */
    fun toDefaultApiError() = toApiError(Error.DefaultApiError.serializer())
    internal inline fun <reified ApiError : Error.ApiError> toApiError(serializer: KSerializer<ApiError>): ApiError? {
        if (responseContentType?.lowercase(Locale.ENGLISH) != "application" || responseContentSubType?.lowercase(
                Locale.ENGLISH
            ) != "json"
        ) {
            return null
        }
        return responseBody?.let { rawString ->
            try {
                json.decodeFromString(serializer, rawString)
            } catch (e: SerializationException) {
                null
            }
        }
    }
}
