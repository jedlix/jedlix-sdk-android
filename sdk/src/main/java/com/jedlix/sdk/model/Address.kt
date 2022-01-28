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
 * An address of the user
 * @property street Street name of the user's address
 * @property postalCode Postalcode of the user's address
 * @property houseNumber House number of the user's address including additions
 * @property city City of the user's address
 * @property region Province or region of the user's address
 * @property country Country in ISO 3166-1 alpha-2 standard
 * @property area Price area or grid region, used when a country has multiple pricing regions for the energy market
 */
@Serializable
data class Address(
    val street: String? = null,
    val postalCode: String? = null,
    val houseNumber: String? = null,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val area: String? = null
)
