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

package com.jedlix.sdk

import android.util.Log
import com.jedlix.sdk.JedlixSDK.LogLevel.ERRORS
import com.jedlix.sdk.connectSession.ConnectSessionObserver
import com.jedlix.sdk.networking.Authentication
import com.jedlix.sdk.networking.Api
import com.jedlix.sdk.networking.KtorApi
import com.jedlix.sdk.networking.endpoint.EndpointBuilder
import java.net.URL
import java.util.concurrent.atomic.AtomicReference

/**
 * Manages the settings of the Jedlix SDK
 */
class JedlixSDK private constructor(
    private val apiHost: String = "",
    private val apiBasePath: String = "",
    private val connectSessionObserver: ConnectSessionObserver,
    private val authentication: Authentication
) {

    /**
     * Sets the log level of the Jedlix SDK. Defaults to [ERRORS]
     */
    enum class LogLevel {

        /**
         * Doesn't log anything
         */
        NONE,

        /**
         * Only logs errors and other unexpected behaviour
         */
        ERRORS,

        /**
         * Logs errors and debug info such as network responses
         */
        ALL
    }

    companion object {
        /**
         * Creates a [JedlixSDK] instance
         * @param apiURL The [URL] of the API
         * @param connectSessionObserver A [ConnectSessionObserver] to observe connect session changes
         * @param Authentication The [authentication] providing the access token to the API
         */
        fun configure(
            apiURL: URL,
            connectSessionObserver: ConnectSessionObserver,
            authentication: Authentication
        ): JedlixSDK {
            if (
                !sdk.compareAndSet(
                    null,
                    JedlixSDK(
                        apiURL.host,
                        apiURL.path.substringBefore(EndpointBuilder().path),
                        connectSessionObserver,
                        authentication
                    )
                )
            ) {
                throw RuntimeException("JedlixSDK can only be configured once")
            }

            return sdk.get()!!
        }

        internal val isConfigured: Boolean get() = sdk.get() != null
        private val sdk = AtomicReference<JedlixSDK?>(null)
        private const val LOG_TAG = "JedlixSDK"

        /**
         * Sets the [LogLevel] of the SDK
         */
        var logLevel: LogLevel = if (BuildConfig.DEBUG) {
            LogLevel.ALL
        } else {
            LogLevel.ERRORS
        }

        /**
         * Provides the [Api] used by the SDK. Make sure [configure] has been called.
         */
        val api: Api
            get() = sdk.get()!!.run {
                KtorApi(
                    apiHost,
                    apiBasePath,
                    authentication
                )
            }

        internal val connectSessionObserver: ConnectSessionObserver
            get() = sdk.get()!!.run { connectSessionObserver }

        internal fun logDebug(message: String) {
            when (logLevel) {
                LogLevel.ALL -> Log.d(LOG_TAG, message)
                LogLevel.ERRORS,
                LogLevel.NONE -> {
                }
            }
        }

        internal fun logError(message: String) {
            when (logLevel) {
                LogLevel.ALL,
                LogLevel.ERRORS -> Log.e(LOG_TAG, message)
                LogLevel.NONE -> {}
            }
        }
    }
}
