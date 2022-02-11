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

package com.jedlix.sdk.viewModel.connectSession

import com.jedlix.sdk.connectSession.ConnectSessionType
import kotlinx.serialization.Serializable

@Serializable
internal sealed class ConnectSessionArguments {

    abstract val userId: String

    @Serializable
    data class Create(
        override val userId: String,
        val connectSessionType: ConnectSessionType
    ) : ConnectSessionArguments()

    @Serializable
    data class Resume(
        override val userId: String,
        val connectSessionId: String
    ) : ConnectSessionArguments()
}
