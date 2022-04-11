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

package com.jedlix.sdk.connectSession

import kotlinx.serialization.Serializable

/**
 * Type of [com.jedlix.sdk.model.ConnectSessionDescriptor] to create
 */
@Serializable
sealed class ConnectSessionType {
    /**
     * Creates a [com.jedlix.sdk.model.VehicleConnectSession] to connect to a [com.jedlix.sdk.model.Vehicle]
     */
    @Serializable
    object Vehicle : ConnectSessionType()

    /**
     * Creates a [com.jedlix.sdk.model.VehicleConnectSession] to connect to a selected [com.jedlix.sdk.model.Vehicle]
     */
    @Serializable
    data class SelectedVehicle(val vehicleId: String) : ConnectSessionType()

    /**
     * Creates a [com.jedlix.sdk.model.ChargerConnectSession]
     * @property chargingLocationId The [com.jedlix.sdk.model.ChargingLocation.id] of the location for which to create a connect session
     */
    @Serializable
    data class Charger(val chargingLocationId: String) : ConnectSessionType()
}
