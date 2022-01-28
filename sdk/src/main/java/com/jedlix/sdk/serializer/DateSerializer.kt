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

package com.jedlix.sdk.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * [KSerializer] for serializing a [Date] as provided by the api
 * Formatted as `yyyy-MM-dd'T'HH:mm:ss.SSSSSS` in UTC
 */
open class DateSerializer(pattern: String, private val timeZone: TimeZone = TimeZone.getDefault()) : KSerializer<Date> {

    private val formatter =
        SimpleDateFormat(pattern, Locale.ENGLISH).apply {
            timeZone = this@DateSerializer.timeZone
        }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.util.Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(formatter.format(value))
    }

    override fun deserialize(decoder: Decoder): Date = formatter.parse(decoder.decodeString())!!
}

class ApiDateSerializer : DateSerializer("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", TimeZone.getTimeZone("UTC"))
class TimeStampSerializer : DateSerializer("yyyy-MM-dd'T'HH:mm:ss", TimeZone.getTimeZone("UTC"))
class TimeSerializer : DateSerializer("HH:mm")
