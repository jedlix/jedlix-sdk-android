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

package com.jedlix.sdk.networking

import com.jedlix.sdk.JedlixSDK
import com.jedlix.sdk.networking.endpoint.EndpointNode
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.errors.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer

internal class KtorApi(
    override val apiHost: String,
    override val basePath: String,
    override val authentication: Authentication
) : Api() {

    private val json = kotlinx.serialization.json.Json {
        isLenient = false
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = false
        useArrayPolymorphism = false
    }

    private val client: HttpClient
        get() = HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = timeoutMillis
                requestTimeoutMillis = timeoutMillis
                socketTimeoutMillis = timeoutMillis
            }
            install(Logging) {
                level = when (JedlixSDK.logLevel) {
                    JedlixSDK.LogLevel.ALL -> LogLevel.ALL
                    else -> LogLevel.NONE
                }
                logger = object : Logger {
                    override fun log(message: String) {
                        JedlixSDK.logDebug(message)
                    }
                }
            }

            addDefaultResponseValidation()
            HttpResponseValidator {
                handleResponseException {
                    throw when (it) {
                        is ResponseException -> {
                            val response = it.response
                            ApiException(
                                response.status.value,
                                response.request.url.fullPath,
                                responseContentType = response.contentType()?.contentType,
                                responseContentSubType = response.contentType()?.contentSubtype,
                                responseBody = response.readText()
                            )
                        }
                        else -> it
                    }
                }
            }
        }

    override suspend fun <Result : Any> request(endpoint: EndpointNode<Result>): Response<Result> {
        return try {
            client.use { client ->
                val response = client.request<HttpResponse> {
                    url {
                        this.protocol = URLProtocol.HTTPS
                        this.host = apiHost
                        this.encodedPath = "$basePath${endpoint.path}"
                        endpoint.query.forEach { parameter ->
                            parameter.value?.let { value ->
                                parameters[parameter.key] = value
                            }
                        }
                    }
                    val headersMap = this@KtorApi.headers()
                    headers {
                        headersMap.forEach { (key, value) -> this[key] = value }
                    }
                    method = when (val method = endpoint.method) {
                        is Method.Get -> HttpMethod.Get
                        is Method.MethodWithBody<*> -> {
                            contentType(ContentType.Application.Json)
                            body = method.body
                            when (method) {
                                is Method.Post<*> -> HttpMethod.Post
                                is Method.Patch<*> -> HttpMethod.Patch
                                is Method.Put<*> -> HttpMethod.Put
                            }
                        }
                        is Method.Delete -> {
                            // Api requires some body on delete requests
                            contentType(ContentType.Application.Json)
                            body = "{}"
                            HttpMethod.Delete
                        }
                        is Method.EmptyPost -> {
                            contentType(ContentType.Application.Json)
                            body = "{}"
                            HttpMethod.Post
                        }
                    }
                }

                val content = when (response.status) {
                    HttpStatusCode.NoContent -> json.encodeToString(Unit.serializer(), Unit)
                    else -> response.readText()
                }

                Response.Success(
                    json.decodeFromString(endpoint.resultDescriptor.serializer, content)
                )
            }
        } catch (e: Throwable) {
            when (e) {
                is ApiException -> {
                    endpoint.resultDescriptor.toError(e)?.let { Response.Error(it) }
                        ?: Response.InvalidResult()
                }
                is SerializationException -> {
                    Response.InvalidResult()
                }
                is IOException -> {
                    Response.NetworkFailure()
                }
                else -> throw e
            }
        }
    }
}
