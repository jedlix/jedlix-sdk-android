/*
 * Copyright 2025 Jedlix B.V. The Netherlands
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

import com.jedlix.sdk.model.ConnectSessionDescriptor

internal sealed class OverrideUrlTypes {
    abstract fun shouldOverride(): Boolean

    companion object {
        fun toUrlType(url: String, session: ConnectSessionDescriptor?): OverrideUrlTypes {
            return when {
                url.startsWith("newtab:") -> NewTab(url)
                session != null -> Session(url, session)
                else -> None
            }
        }
    }

    data class NewTab(private val newTabUrl: String) : OverrideUrlTypes() {
        override fun shouldOverride(): Boolean = true
        val url = removeNewTabPrefix(newTabUrl)

        private fun removeNewTabPrefix(url: String): String = url.replace("newtab:", "")
    }
    data class Session(val url: String, val session: ConnectSessionDescriptor) : OverrideUrlTypes() {
        override fun shouldOverride(): Boolean {
            val redirectUrl = session.redirectUrl
            return redirectUrl != null && url.startsWith(redirectUrl)
        }
    }
    object None : OverrideUrlTypes() {
        override fun shouldOverride(): Boolean = false
    }
}