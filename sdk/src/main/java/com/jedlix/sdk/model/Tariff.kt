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

package com.jedlix.sdk.model

import com.jedlix.sdk.serializer.EnumSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tariff
 * @property currency Currency according to the ISO 4217 standard. Can be left empty in case of a [Type.DYNAMIC] [type].
 * @property price Price per kWh in the given [currency]. For [Type.DYNAMIC] [type] set this to zero.
 * @property type The [Tariff.Type] of tariff this is.
 */
@Serializable
data class Tariff(
    val currency: String? = null,
    val price: Double,
    @Serializable(with = Type.Serializer::class)
    val type: Type
) {
    /**
     * You can have a couple of combinations in a list of [Tariff] list. Each type can only be added once. [Type.DYNAMIC] and [Type.STANDARD] cannot be used in combination with other types. [Type.PEAK], [Type.OFF_PEAK] and [Type.SUPER_OFF_PEAK] can be used with each other.
     */
    @Serializable
    enum class Type {
        @SerialName("standard")
        STANDARD,
        @SerialName("peak")
        PEAK,
        @SerialName("offPeak")
        OFF_PEAK,
        @SerialName("superOffPeak")
        SUPER_OFF_PEAK,
        @SerialName("dynamic")
        DYNAMIC,
        @SerialName("unknown")
        UNKNOWN;

        class Serializer : EnumSerializer<Type>(UNKNOWN, serializer())
    }
}
