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

package com.jedlix.sdk.example.viewModel

import com.jedlix.sdk.example.model.Button
import com.jedlix.sdk.model.Charger
import com.jedlix.sdk.model.ChargerConnectSession
import com.jedlix.sdk.model.ChargingLocation

class ChargingLocationViewModel(
    private val chargingLocation: ChargingLocation,
    chargers: List<Charger>,
    private val chargerSessions: List<ChargerConnectSession>,
    private val startChargerConnectSession: (ChargingLocation) -> Unit,
    private val resumeConnectSession: (ChargerConnectSession) -> Unit,
    private val removeCharger: (Charger) -> Unit
) : GroupedConnectSessionViewModel {

    override val title = chargingLocation.address.let { listOfNotNull(it.street, it.houseNumber, it.city).joinToString(", ") }
    override val connectSessionViewModels = chargers.map { charger ->
        ChargerViewModel(charger, chargerSessions.firstOrNull { it.chargerId == charger.id }, resumeConnectSession, removeCharger)
    }.ifEmpty {
        val connectSessionWithoutVehicle = chargerSessions.firstOrNull { it.chargingLocationId == chargingLocation.id && it.chargerId.isNullOrEmpty() }
        listOf(
            object : ConnectSessionViewModel {
                override val title: String = if (connectSessionWithoutVehicle != null) "Unfinished connect session" else "No Chargers found"
                override val buttons: List<Button> = listOf(
                    connectSessionWithoutVehicle?.let { Button("Resume") { resumeConnectSession(it) } } ?: Button("Connect") { startChargerConnectSession(chargingLocation) }
                )
            }
        )
    }
}
