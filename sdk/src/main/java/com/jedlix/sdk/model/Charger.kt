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
 * A charger placed at a [ChargingLocation] for charging a [Vehicle]
 * @property id Identifier of the charger.
 * @property chargingLocationId [ChargingLocation.id] of the charger.
 * @property detail The [Charger.Detail] of the charger.
 * @property chargerState The [ChargerState] of the charger.
 */
@Serializable
data class Charger(
    val id: String,
    val chargingLocationId: String,
    val detail: Detail,
    val chargerState: ChargerState
) {
    /**
     * Details of a [Charger]
     * @property brand The brand of the charger
     * @property model The model or version of the charger
     */
    @Serializable
    data class Detail(
        val brand: String?,
        val model: String?
    )
}
