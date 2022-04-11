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
 * A session to handle the connection process to a [ConnectSessionDescriptor.Settings.Type]
 * @property id The id of this session
 * @property isFinished Tells if the entire flow is done. If `true`, no extra pages need loading and the sdk can give the control back to the calling application
 * @property startUrl Start Url to load by the web view
 * @property redirectUrl The start of the redirect url, so when the next loaded url is this, then the web view for this flow can be closed, and next one can be requested.
 * @property redirectInfo The [ConnectSessionDescriptor.RedirectInfo] used when a url matches the [redirectUrl]
 */
sealed interface ConnectSessionDescriptor {

    val id: String
    val isFinished: Boolean
    val startUrl: String?
    val redirectUrl: String?
    val redirectInfo: RedirectInfo?

    /**
     * Explains how to create an [Info] response from a webview when matching the [ConnectSessionDescriptor.redirectUrl]
     * @property includeBody if `true` the body should be posted back in [Info.body]
     * @property includeCookies The list names of the cookies that should be posted back in [Info.cookies]
     * @property includeRedirectUrl If `true` the redirect url should be posted back in [Info.redirectUrl]
     */
    @Serializable
    data class RedirectInfo(
        val includeBody: Boolean = false,
        val includeCookies: List<String> = emptyList(),
        val includeRedirectUrl: Boolean = false
    )

    /**
     * Info of data from an externally controlled website used for the [ConnectSessionDescriptor]
     * @property body The full body of loading this url
     * @property cookies The cookies requested by the request parameters of the web view session model
     * @property redirectUrl The full redirect url from the request
     */
    @Serializable
    data class Info(
        val body: String?,
        val cookies: Map<String, String> = emptyMap(),
        val redirectUrl: String? = null
    )
}

@Serializable
data class ConnectSession(
    override val id: String,
    override val isFinished: Boolean,
    override val startUrl: String? = null,
    override val redirectUrl: String? = null,
    override val redirectInfo: ConnectSessionDescriptor.RedirectInfo? = null
) : ConnectSessionDescriptor

/**
 * A [ConnectSessionDescriptor] for a [Vehicle]
 * @property vehicleId The [Vehicle.id] of the vehicle being connected
 */
@Serializable
data class VehicleConnectSession(
    override val id: String,
    val vehicleId: String? = null,
    override val isFinished: Boolean,
    override val startUrl: String? = null,
    override val redirectUrl: String? = null,
    override val redirectInfo: ConnectSessionDescriptor.RedirectInfo? = null
) : ConnectSessionDescriptor

/**
 * A [ConnectSessionDescriptor] for a Charger
 * @property chargerId The id of the Charger
 * @property chargingLocationId The location of the charger
 */
@Serializable
data class ChargerConnectSession(
    override val id: String,
    val chargerId: String? = null,
    val chargingLocationId: String,
    override val isFinished: Boolean,
    override val startUrl: String? = null,
    override val redirectUrl: String? = null,
    override val redirectInfo: ConnectSessionDescriptor.RedirectInfo? = null
) : ConnectSessionDescriptor
