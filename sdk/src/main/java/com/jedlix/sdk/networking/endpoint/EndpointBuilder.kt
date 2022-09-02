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

import com.jedlix.sdk.model.*
import com.jedlix.sdk.networking.ApiException
import com.jedlix.sdk.networking.Error
import com.jedlix.sdk.networking.Method
import kotlinx.serialization.KSerializer

/**
 * Path of an endpoint
 */
interface EndpointPath {
    /**
     * String representing the path
     */
    val path: String
}

/**
 * And [EndpointPath] that van be requested to receive a [Result]
 * @param Result The expected type of the result provided by this endpoint node
 */
sealed interface EndpointNode<Result> : EndpointPath {
    /**
     * The [Method] used when calling this endpoint node
     */
    val method: Method

    /**
     * The [EndpointResultDescriptor] describing the result expected by this endpoint
     */
    val resultDescriptor: EndpointResultDescriptor<Result>

    /**
     * The parameters provided in a query to the endpoint
     */
    val query: Map<String, String?> get() = emptyMap()
}

/**
 * Described the result data of an [EndpointNode]
 * @param Result the type of the result
 */
sealed interface EndpointResultDescriptor<Result> {
    /**
     * [KSerializer] to deserialize [Result]
     */
    val serializer: KSerializer<Result>

    /**
     * Mapper function that tries to map an [ApiException] into an [Error] expected by the endpoint
     */
    fun toError(apiException: ApiException): Error?
}

/**
 * Builder for generating [EndpointNode]
 */
class EndpointBuilder internal constructor() : EndpointPath {
    override val path: String = "/api/v1"

    /**
     * All endpoints in the `users` domain
     */
    inner class Users : EndpointPath {
        override val path: String = "${this@EndpointBuilder.path}/users"

        /**
         * All endpoints in the `user` domain
         * @param userId The identifier of the user requested by this [EndpointPath]
         */
        inner class User(userId: String) : EndpointPath {
            override val path: String = "${this@Users.path}/$userId"

            inner class ChargingLocations : EndpointPath {
                override val path: String = "${this@User.path}/charging-locations"

                inner class Get(area: String? = null, region: String? = null, country: String? = null) : EndpointNode<List<com.jedlix.sdk.model.ChargingLocation>> {
                    override val path: String = this@ChargingLocations.path
                    override val method: Method = Method.Get
                    override val resultDescriptor: EndpointResultDescriptor<List<com.jedlix.sdk.model.ChargingLocation>> = ChargingLocationListDescriptor
                    override val query: Map<String, String?> = mapOf(
                        "area" to area,
                        "region" to region,
                        "country" to country
                    )
                }

                inner class ChargingLocation(chargingLocationId: String) : EndpointPath {
                    override val path: String = "${this@ChargingLocations.path}/$chargingLocationId"

                    inner class Get : EndpointNode<com.jedlix.sdk.model.ChargingLocation> {
                        override val path: String = this@ChargingLocation.path
                        override val method: Method = Method.Get
                        override val resultDescriptor: EndpointResultDescriptor<com.jedlix.sdk.model.ChargingLocation> =
                            ChargingLocationDescriptor
                    }

                    inner class Delete : EndpointNode<Unit> {
                        override val path: String = this@ChargingLocation.path
                        override val method: Method = Method.Get
                        override val resultDescriptor: EndpointResultDescriptor<Unit> = ChargingLocationDeleteDescriptor
                    }

                    inner class Chargers : EndpointPath {
                        override val path: String = "${this@ChargingLocation.path}/chargers"

                        inner class Get : EndpointNode<List<com.jedlix.sdk.model.Charger>> {
                            override val path: String = this@Chargers.path
                            override val method: Method = Method.Get
                            override val resultDescriptor: EndpointResultDescriptor<List<com.jedlix.sdk.model.Charger>> =
                                ChargerListDescriptor
                        }

                        inner class Charger(chargerId: String) : EndpointPath {
                            override val path: String = "${this@Chargers.path}/$chargerId"

                            inner class Get : EndpointNode<com.jedlix.sdk.model.Charger> {
                                override val path: String = this@Charger.path
                                override val method: Method = Method.Get
                                override val resultDescriptor: EndpointResultDescriptor<com.jedlix.sdk.model.Charger> = ChargerDescriptor
                            }

                            inner class Delete : EndpointNode<Unit> {
                                override val path: String = this@Charger.path
                                override val method: Method = Method.Delete
                                override val resultDescriptor: EndpointResultDescriptor<Unit> = ChargerDeleteDescriptor
                            }

                            inner class State : EndpointNode<ChargerState> {
                                override val path: String = "${this@Charger.path}/state"
                                override val method: Method = Method.Get
                                override val resultDescriptor: EndpointResultDescriptor<ChargerState> = ChargerStateDescriptor
                            }
                        }

                        internal inner class StartConnectSession : EndpointNode<ChargerConnectSession> {
                            override val path: String = "${this@Chargers.path}/connect-sessions"
                            override val method: Method = Method.EmptyPost
                            override val resultDescriptor: EndpointResultDescriptor<ChargerConnectSession> = ConnectSessionsDescriptor.Create(ChargerConnectSession.serializer())
                        }
                    }
                }
            }

            inner class Chargers : EndpointPath {
                override val path: String = "${this@User.path}/chargers"

                inner class Get : EndpointNode<List<Charger>> {
                    override val path: String = this@Chargers.path
                    override val method: Method = Method.Get
                    override val resultDescriptor: EndpointResultDescriptor<List<Charger>> = ChargerListDescriptor
                }
            }

            /**
             * All endpoints in the `connect-sessions` domain
             */
            inner class ConnectSessions : EndpointPath {
                override val path: String = "${this@User.path}/connect-sessions"

                /**
                 * All endpoints in the `session` domain
                 * @param sessionId The identifier of the [ConnectSessionDescriptor]
                 */
                inner class Session(sessionId: String) : EndpointPath {
                    override val path: String = "${this@ConnectSessions.path}/$sessionId"

                    /**
                     * [EndpointNode] that retrieves specified [ConnectSessionDescriptor]. This is handled within the SDK
                     */
                    inner class Get : EndpointNode<ConnectSession> {
                        override val path: String = this@Session.path
                        override val method: Method = Method.Get
                        override val resultDescriptor: EndpointResultDescriptor<ConnectSession> = ConnectSessionsDescriptor.Session(ConnectSession.serializer())
                    }

                    /**
                     * This is an [EndpointNode] that allows the sdk to send an [ConnectSessionDescriptor.Info] update with the data of externally controlled websites.
                     * This is handled within the SDK
                     */
                    inner class Info(connectSessionInfo: ConnectSessionDescriptor.Info) :
                        EndpointNode<ConnectSession> {
                        override val path: String = "${this@Session.path}/info"
                        override val method: Method = object : Method.Post<ConnectSessionDescriptor.Info> {
                            override val body: ConnectSessionDescriptor.Info = connectSessionInfo
                        }
                        override val resultDescriptor: EndpointResultDescriptor<ConnectSession> = ConnectSessionsDescriptor.Session(ConnectSession.serializer())
                    }
                }

                /**
                 * Retrieves all unfinished [VehicleConnectSession] for the user.
                 */
                inner class GetVehicleConnectSessions : EndpointNode<List<VehicleConnectSession>> {
                    override val path: String = this@ConnectSessions.path
                    override val method: Method = Method.Get
                    override val query: Map<String, String?> = mapOf("type" to "vehicle")
                    override val resultDescriptor: EndpointResultDescriptor<List<VehicleConnectSession>> = ConnectSessionListDescriptor(VehicleConnectSession.serializer())
                }

                /**
                 * Retrieves all unfinished [ChargerConnectSession] for the user.
                 */
                inner class GetChargerConnectSessions : EndpointNode<List<ChargerConnectSession>> {
                    override val path: String = this@ConnectSessions.path
                    override val method: Method = Method.Get
                    override val query: Map<String, String?> = mapOf("type" to "charger")
                    override val resultDescriptor: EndpointResultDescriptor<List<ChargerConnectSession>> = ConnectSessionListDescriptor(ChargerConnectSession.serializer())
                }
            }

            /**
             * All endpoints in the `vehicles` domain
             */
            inner class Vehicles : EndpointPath {
                override val path: String = "${this@User.path}/vehicles"

                /**
                 * [EndpointNode] that retrieves a list of all [Vehicle] of a user
                 */
                inner class Get : EndpointNode<List<com.jedlix.sdk.model.Vehicle>> {
                    override val path: String = this@Vehicles.path
                    override val method: Method = Method.Get
                    override val resultDescriptor: EndpointResultDescriptor<List<com.jedlix.sdk.model.Vehicle>> = VehicleListDescriptor
                }

                /**
                 * All endpoints in the `vehicle` domain
                 * @param vehicleId The [com.jedlix.sdk.model.Vehicle.id] of the [com.jedlix.sdk.model.Vehicle]
                 */
                inner class Vehicle(vehicleId: String) : EndpointPath {
                    override val path: String = "${this@Vehicles.path}/$vehicleId"

                    /**
                     * [EndpointNode] that retrieves the details of a [com.jedlix.sdk.model.Vehicle]
                     */
                    inner class Get : EndpointNode<com.jedlix.sdk.model.Vehicle> {
                        override val path: String = this@Vehicle.path
                        override val method: Method = Method.Get
                        override val resultDescriptor: EndpointResultDescriptor<com.jedlix.sdk.model.Vehicle> = VehicleDescriptor
                    }

                    /**
                     * [EndpointNode] that creates a [VehicleConnectSession] to connect a specified [com.jedlix.sdk.model.Vehicle]
                     */
                    internal inner class StartConnectSession : EndpointNode<VehicleConnectSession> {
                        override val path: String = "${this@Vehicle.path}/connect-sessions"
                        override val method: Method = Method.EmptyPost
                        override val resultDescriptor: EndpointResultDescriptor<VehicleConnectSession> = ConnectSessionsDescriptor.Create(VehicleConnectSession.serializer())
                    }

                    /**
                     * [EndpointNode] that removes a [com.jedlix.sdk.model.Vehicle]
                     */
                    inner class Delete : EndpointNode<Unit> {
                        override val path: String = this@Vehicle.path
                        override val method: Method = Method.Delete
                        override val resultDescriptor: EndpointResultDescriptor<Unit> = VehicleDeleteDescriptor
                    }
                }

                internal inner class StartConnectSession : EndpointNode<ConnectSession> {
                    override val path: String = "${this@Vehicles.path}/connect-sessions"
                    override val method: Method = Method.EmptyPost
                    override val resultDescriptor: EndpointResultDescriptor<ConnectSession> = ConnectSessionsDescriptor.Create(ConnectSession.serializer())
                }
            }
        }
    }
}
