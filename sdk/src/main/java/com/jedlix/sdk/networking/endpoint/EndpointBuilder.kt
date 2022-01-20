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

import com.jedlix.sdk.model.ConnectSession
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
    val query: Map<String, String> get() = emptyMap()
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
    override val path: String = "/api/v01-01"

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

            /**
             * All endpoints in the `connect-sessions` domain
             */
            inner class ConnectSessions : EndpointPath {
                override val path: String = "${this@User.path}/connect-sessions"

                /**
                 * An [EndpointNode] for creating a new [ConnectSession]
                 * This is handled within the SDK
                 */
                inner class Create(settings: ConnectSession.Settings) : EndpointNode<ConnectSession> {
                    override val path: String = this@ConnectSessions.path
                    override val method: Method = object : Method.Post<ConnectSession.Settings> {
                        override val body = settings
                    }
                    override val resultDescriptor: EndpointResultDescriptor<ConnectSession> = ConnectSessionsDescriptor.Create
                }

                /**
                 * All endpoints in the `session` domain
                 * @param sessionId The identifier of the [ConnectSession]
                 */
                inner class Session(sessionId: String) : EndpointPath {
                    override val path: String = "${this@ConnectSessions.path}/$sessionId"

                    /**
                     * [EndpointNode] that retrieves specified [ConnectSession]. This is handled within the SDK
                     */
                    inner class Get : EndpointNode<ConnectSession> {
                        override val path: String = this@Session.path
                        override val method: Method = Method.Get
                        override val resultDescriptor: EndpointResultDescriptor<ConnectSession> = ConnectSessionsDescriptor.Session
                    }

                    /**
                     * This is an [EndpointNode] that allows the sdk to send an [ConnectSession.Info] update with the data of externally controlled websites.
                     * This is handled within the SDK
                     */
                    inner class Info(connectSessionInfo: ConnectSession.Info) :
                        EndpointNode<ConnectSession> {
                        override val path: String = "${this@Session.path}/info"
                        override val method: Method = object : Method.Post<ConnectSession.Info> {
                            override val body: ConnectSession.Info = connectSessionInfo
                        }
                        override val resultDescriptor: EndpointResultDescriptor<ConnectSession> = ConnectSessionsDescriptor.Session
                    }
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
                     * [EndpointNode] that removes a [com.jedlix.sdk.model.Vehicle]
                     */
                    inner class Delete : EndpointNode<Unit> {
                        override val path: String = this@Vehicle.path
                        override val method: Method = Method.Delete
                        override val resultDescriptor: EndpointResultDescriptor<Unit> = VehicleDeleteDescriptor
                    }
                }
            }
        }
    }
}
