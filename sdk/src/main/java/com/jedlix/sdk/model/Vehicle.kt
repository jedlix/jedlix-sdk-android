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

import com.jedlix.sdk.serializer.ApiDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Describes a vehicle that is connected to Jedlix
 * @property id The identifier of the vehicle
 * @property createdAt The [Date] in UTC when the vehicle was created
 * @property details The [Details] of the vehicle
 * @property isConnected Indicates whether this vehicle is connected or not
 * @property capabilities List of [Capabilities] supported by the vehicle
 * @property chargeState The [VehicleChargeState] of the vehicle
 */
@Serializable
class Vehicle(
    val id: String,
    @Serializable(with = ApiDateSerializer::class)
    val createdAt: Date,
    val details: Details,
    val isConnected: Boolean,
    val capabilities: List<Capabilities>? = null,
    val chargeState: VehicleChargeState? = null
) {
    /**
     * Details of the vehicle
     * @property brand Brand name of the vehicle
     * @property model Model name of the vehicle
     * @property version The model version of the vehicle
     */
    @Serializable
    data class Details(
        val brand: String? = null,
        val model: String? = null,
        val version: String? = null
    )

    /**
     * The capabilities of a [Vehicle]
     */
    @Serializable
    enum class Capabilities {
        @SerialName("chargeState")
        CHARGE_STATE,

        @SerialName("stateOfCharge")
        STATE_OF_CHARGE,

        @SerialName("charge")
        CHARGE,

        @SerialName("discharge")
        DISCHARGE,

        @SerialName("geoLocation")
        GEO_LOCATION,

        @SerialName("desiredStateOfCharge")
        DESIRED_STATE_OF_CHARGE,

        @SerialName("pairing")
        PAIRING,

        @SerialName("departureTimes")
        DEPARTURE_TIMES,

        @SerialName("pullTelemetry")
        PULL_TELEMETRY,

        @SerialName("pushTelemetry")
        PUSH_TELEMETRY,

        @SerialName("geoFencing")
        GEO_FENCING
    }
}
