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

import com.jedlix.sdk.model.ChargingLocation
import com.jedlix.sdk.networking.ApiException
import com.jedlix.sdk.networking.Error
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

internal object ChargingLocationDescriptor : EndpointResultDescriptor<ChargingLocation> {
    override val serializer = ChargingLocation.serializer()
    override fun toError(apiException: ApiException): Error? =
        when (apiException.code) {
            HttpStatusCode.Unauthorized.value -> Error.Unauthorized
            HttpStatusCode.Forbidden.value -> Error.Forbidden
            HttpStatusCode.NotFound.value -> apiException.toDefaultApiError()
            else -> null
        }
}

internal object ChargingLocationListDescriptor : EndpointResultDescriptor<List<ChargingLocation>> {
    override val serializer = ListSerializer(ChargingLocation.serializer())
    override fun toError(apiException: ApiException): Error? =
        when (apiException.code) {
            HttpStatusCode.Unauthorized.value -> Error.Unauthorized
            HttpStatusCode.Forbidden.value -> Error.Forbidden
            else -> null
        }
}

internal object ChargingLocationDeleteDescriptor : EndpointResultDescriptor<Unit> {
    override val serializer: KSerializer<Unit> = Unit.serializer()
    override fun toError(apiException: ApiException): Error? =
        when (apiException.code) {
            HttpStatusCode.Unauthorized.value -> Error.Unauthorized
            HttpStatusCode.Forbidden.value -> Error.Forbidden
            HttpStatusCode.NotFound.value -> apiException.toDefaultApiError()
            else -> null
        }
}
