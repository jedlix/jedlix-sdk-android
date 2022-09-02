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
import com.jedlix.sdk.model.Vehicle
import com.jedlix.sdk.model.VehicleConnectSession

class VehicleViewModel(
    vehicle: Vehicle,
    connectSession: VehicleConnectSession?,
    private val resumeConnectSession: (VehicleConnectSession) -> Unit,
    private val removeVehicle: (Vehicle) -> Unit
) : ConnectSessionViewModel {

    override val title = vehicle.details.fullName
    override val buttons = listOfNotNull(
        connectSession?.let { Button("Resume") { resumeConnectSession(it) } },
        Button("Remove") { removeVehicle(vehicle) }
    )

    private val Vehicle.Details.fullName: String get() = listOfNotNull(brand, model, version).joinToString(" ")
}
