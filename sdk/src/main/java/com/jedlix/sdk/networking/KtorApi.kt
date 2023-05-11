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
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.addDefaultResponseValidation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.fullPath
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer

internal class KtorApi(
    override val host: String,
    override val basePath: String,
    override val apiKey: String?,
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
            install(ContentNegotiation) {
                json(json)
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
                handleResponseExceptionWithRequest { exception, _ ->
                    throw when (exception) {
                        is ResponseException -> {
                            val response = exception.response
                            ApiException(
                                response.status.value,
                                response.request.url.fullPath,
                                responseContentType = response.contentType()?.contentType,
                                responseContentSubType = response.contentType()?.contentSubtype,
                                responseBody = response.bodyAsText()
                            )
                        }
                        else -> exception
                    }
                }
            }
        }

    override suspend fun <Result : Any> request(endpoint: EndpointNode<Result>): Response<Result> {
        return try {
            client.use { client ->
                val apiHost = host
                val response = client.request {
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
                            setBody(method.body)
                            when (method) {
                                is Method.Post<*> -> HttpMethod.Post
                                is Method.Patch<*> -> HttpMethod.Patch
                                is Method.Put<*> -> HttpMethod.Put
                            }
                        }
                        is Method.Delete -> {
                            // Api requires some body on delete requests
                            contentType(ContentType.Application.Json)
                            setBody("{}")
                            HttpMethod.Delete
                        }
                        is Method.EmptyPost -> {
                            contentType(ContentType.Application.Json)
                            setBody("{}")
                            HttpMethod.Post
                        }
                    }
                }

                val content = when (response.status) {
                    HttpStatusCode.NoContent -> json.encodeToString(Unit.serializer(), Unit)
                    else -> response.bodyAsText()
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
