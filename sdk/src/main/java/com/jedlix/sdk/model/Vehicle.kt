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

import com.jedlix.sdk.model.Vehicle.Capabilities
import com.jedlix.sdk.model.Vehicle.Details
import com.jedlix.sdk.serializer.ApiDateSerializer
import com.jedlix.sdk.serializer.EnumSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 * Describes a vehicle that is connected to Jedlix
 * @property id The identifier of the vehicle
 * @property createdAt The [Date] in UTC when the vehicle was created
 * @property details The [Details] of the vehicle
 * @property isConnected Indicates whether this vehicle is connected or not
 * @property isConnectable Indicates whether this vehicle is connectable
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
    val isConnectable: Boolean,
    val capabilities: Capabilities.List? = null,
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

        @SerialName("startStopCharging")
        START_STOP_CHARGING,

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
        GEO_FENCING,

        @SerialName("unknown")
        UNKNOWN;

        @Serializable(with = ListSerializer::class)
        data class List(val list: kotlin.collections.List<Capabilities>) : kotlin.collections.List<Capabilities> by list
        class Serializer : EnumSerializer<Capabilities>(
            UNKNOWN,
            serializer()
        )
        class ListSerializer : KSerializer<List> {
            private val internal = ListSerializer(Serializer())
            override val descriptor: SerialDescriptor = internal.descriptor
            override fun serialize(encoder: Encoder, value: List) = internal.serialize(encoder, value)
            override fun deserialize(decoder: Decoder): List = List(internal.deserialize(decoder))
        }
    }
}
