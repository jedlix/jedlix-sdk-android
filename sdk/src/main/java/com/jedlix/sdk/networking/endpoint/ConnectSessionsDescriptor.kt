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

package com.jedlix.sdk.networking.endpoint

import com.jedlix.sdk.model.ConnectSession
import com.jedlix.sdk.networking.ApiException
import com.jedlix.sdk.networking.Error
import io.ktor.http.*
import kotlinx.serialization.KSerializer

internal sealed class ConnectSessionsDescriptor : EndpointResultDescriptor<ConnectSession> {
    override val serializer: KSerializer<ConnectSession> = ConnectSession.serializer()

    object Create : ConnectSessionsDescriptor() {
        override fun toError(apiException: ApiException): Error? =
            when (apiException.code) {
                HttpStatusCode.BadRequest.value -> apiException.toDefaultApiError()
                HttpStatusCode.Unauthorized.value -> Error.Unauthorized
                HttpStatusCode.Forbidden.value -> Error.Forbidden
                HttpStatusCode.NotFound.value -> apiException.toDefaultApiError()
                else -> null
            }
    }

    object Session : ConnectSessionsDescriptor() {
        override fun toError(apiException: ApiException): Error? =
            when (apiException.code) {
                HttpStatusCode.Unauthorized.value -> Error.Unauthorized
                HttpStatusCode.Forbidden.value -> Error.Forbidden
                HttpStatusCode.NotFound.value -> apiException.toDefaultApiError()
                else -> null
            }
    }
}
