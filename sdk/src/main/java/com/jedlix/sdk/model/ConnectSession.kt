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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A session to handle the connection process to a [ConnectSession.Settings.Type]
 * @property id The id of this session
 * @property isFinished Tells if the entire flow is done. If `true`, no extra pages need loading and the sdk can give the control back to the calling application
 * @property startUrl Start Url to load by the web view
 * @property redirectUrl The start of the redirect url, so when the next loaded url is this, then the web view for this flow can be closed, and next one can be requested.
 * @property redirectInfo The [ConnectSession.RedirectInfo] used when a url matches the [redirectUrl]
 */
@Serializable
data class ConnectSession(
    val id: String,
    val isFinished: Boolean,
    val startUrl: String? = null,
    val redirectUrl: String? = null,
    val redirectInfo: RedirectInfo? = null
) {

    /**
     * Settings used to create a [ConnectSession]
     * @property chargingLocationId Location identifier when [type] is [Type.HomeCharger]
     * @property type The [Type] of the [ConnectSession] to create
     */
    @Serializable
    data class Settings(
        val chargingLocationId: String? = null,
        val type: Type
    ) {
        /**
         * Type of a [ConnectSession]
         */
        @Serializable
        enum class Type {

            /**
             * Connects to a vehicle
             */
            @SerialName("vehicle")
            Vehicle,

            /**
             * Connects to a home charger box
             */
            @SerialName("homeCharger")
            HomeCharger
        }
    }

    /**
     * Explains how to create an [Info] response from a webview when matching the [ConnectSession.redirectUrl]
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
     * Info of data from an externally controlled website used for the [ConnectSession]
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
