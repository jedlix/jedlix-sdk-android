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

package com.jedlix.sdk.example

import android.app.Application
import com.jedlix.sdk.JedlixSDK
import com.jedlix.sdk.example.authentication.Auth0Authentication
import com.jedlix.sdk.example.authentication.Authentication
import com.jedlix.sdk.example.authentication.DefaultAuthentication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.net.URL

class ExampleApplication : Application() {
    companion object {
        lateinit var baseURL: URL
        lateinit var apiKey: String
        lateinit var authentication: Authentication
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        baseURL = URL("<YOUR BASE URL>")
        apiKey = "<YOUR API KEY>"
        authentication = DefaultAuthentication(this)
//        authentication = Auth0Authentication(
//            "<AUTH0 CLIENT ID>",
//            "<AUTH0 DOMAIN>",
//            "<AUTH0 AUDIENCE>",
//            "<USER IDENTIFIER KEY>",
//            coroutineScope,
//            this
//        )
        JedlixSDK.configure(
            baseURL,
            apiKey,
            authentication
        )
    }

    override fun onLowMemory() {
        super.onLowMemory()
        coroutineScope.cancel()
    }
}
