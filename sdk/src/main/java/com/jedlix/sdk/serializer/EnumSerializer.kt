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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

open class EnumSerializer<E>(private val defaultValue: E, private val internalSerializer: KSerializer<E>) :
    KSerializer<E> where E : Enum<E> {

    private val className = this::class.qualifiedName!!
    override val descriptor = internalSerializer.descriptor
    override fun serialize(encoder: Encoder, value: E) = internalSerializer.serialize(encoder, value)

    override fun deserialize(decoder: Decoder) = try {
        internalSerializer.deserialize(decoder)
    } catch (e: SerializationException) {
        defaultValue
    }
}
