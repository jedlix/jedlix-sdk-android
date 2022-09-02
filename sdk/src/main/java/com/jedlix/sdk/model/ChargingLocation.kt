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
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Charging Locations are physical locations where an electric vehicle can be charged.
 * Smart Charging is active at these locations only, this prevents Jedlix to stop charging when the vehicle is not at the charge location.
 * Charging location objects are linked to a user account.
 * The Charging Location includes address details and may contain details on tariffs that apply to that location.
 * The API allows you to create, delete, and update charging locations.
 * You can retrieve details on individual charging locations as well as a list of all charging locations associated with the user.
 * @property id Identifier of the charging location
 * @property userId UserId of the charging location
 * @property coordinates [Coordinates] of the charging location
 * @property address [Address] of the charging location
 * @property energySupplierDetails [EnergySupplierDetails] of the charging location
 * @property tariffDetails [Tariff] of the charging location
 * @property chargers List of [Charger] at the charging location
 * @property createdAt [Date] the charging location has been created in UTC
 * @property updatedAt [Date] the charging location has been updated in UTC
 */
@Serializable
data class ChargingLocation(
    val id: String,
    val userId: String,
    val coordinates: Coordinates,
    val address: Address,
    val energySupplierDetails: EnergySupplierDetails?,
    val tariffDetails: Tariff?,
    val chargers: List<Charger> = emptyList(),
    @Serializable(with = ApiDateSerializer::class)
    val createdAt: Date,
    @Serializable(with = ApiDateSerializer::class)
    val updatedAt: Date
) {
    /**
     * Tariff applied to a [ChargingLocation]
     * @property chargingLocationId The [ChargingLocation.id] of the location this tariff applies to
     * @property tariffs List of [com.jedlix.sdk.model.Tariff] of the charging location
     * @property tariffIntervals List of [TariffInterval] of the charging location tariff
     * @property holidayTariffIntervals List of [TariffInterval] of the charging location tariff applied during holidays
     */
    @Serializable
    data class Tariff(
        val chargingLocationId: String,
        val tariffs: List<com.jedlix.sdk.model.Tariff> = emptyList(),
        val tariffIntervals: List<TariffInterval> = emptyList(),
        val holidayTariffIntervals: List<TariffInterval> = emptyList()
    )
}
