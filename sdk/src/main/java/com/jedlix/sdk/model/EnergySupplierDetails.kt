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

import kotlinx.serialization.Serializable

/**
 * An energy supplier at a [ChargingLocation] associated with a user
 * @property chargingLocationId [ChargingLocation.id] of the charging location
 * @property name Name of the energy supplier
 * @property energySupplierId Identifier of the energy supplier
 * @property contractNumber Number of the contract between the user and the energy supplier
 * @property meterCode Metercode for the connection
 */
@Serializable
data class EnergySupplierDetails(
    val chargingLocationId: String,
    val name: String? = null,
    val energySupplierId: String,
    val contractNumber: String? = null,
    val meterCode: String? = null
)
