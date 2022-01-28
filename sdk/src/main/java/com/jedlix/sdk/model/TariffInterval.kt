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

import com.jedlix.sdk.serializer.TimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Tariff interval at which a [Tariff] is applied
 * @property startTime Local start time of the tariff interval, format like 01:35
 * @property endTime Local end time of the tariff interval, format like 01:35
 * @property daysOfWeek List of [DayOfWeek] of the tariff interval
 * @property tariffType The [Tariff.Type] this interval is
 */
@Serializable
data class TariffInterval(
    @Serializable(with = TimeSerializer::class)
    val startTime: Date? = null,
    @Serializable(with = TimeSerializer::class)
    val endTime: Date? = null,
    val daysOfWeek: List<DayOfWeek>,
    val tariffType: Tariff.Type
) {
    /**
     * Day(s) of the week of the tariff interval
     */
    @Serializable
    enum class DayOfWeek {
        @SerialName("sunday")
        SUNDAY,
        @SerialName("monday")
        MONDAY,
        @SerialName("tuesday")
        TUESDAY,
        @SerialName("wednesday")
        WEDNESDAY,
        @SerialName("thursday")
        THURSDAY,
        @SerialName("friday")
        FRIDAY,
        @SerialName("saturday")
        SATURDAY,
    }
}
