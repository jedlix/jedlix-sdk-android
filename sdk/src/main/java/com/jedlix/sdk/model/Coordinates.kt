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
 * Geographic coordinates
 * @property latitude Geographic coordinate that specifies the north-south position on the earth's surface. Ranges from -90 at the south pole to 90 at the north pole, with 0 being the equator.
 * @property longitude Geographic coordinate that specifies the east-west position on the earth's surface. Ranges from -180 to +180, with 0 being the prime meridian at Greenwich. Positive longitudes are east of the prime meridian and negative ones are west.
 */
@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)
