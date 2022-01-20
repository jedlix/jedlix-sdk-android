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

package com.jedlix.sdk.example.connectSessionObserver

import android.content.Context
import android.content.SharedPreferences
import com.jedlix.sdk.connectSession.ConnectSessionObserver

/**
 * This example version of [ConnectSessionObserver] stores all active sessions in the shared preferences
 * It is recommended not to do this but store the session on a remote location so that the session can be restored even if the app is uninstalled.
 */
class StorageConnectSessionObserver(private val context: Context) : ConnectSessionObserver {

    companion object {
        private const val PREFERENCE_FILE_KEY = "com.jedlix.api.CONNECTION_SESSION_STORAGE"
        private const val CONNECT_SESSIONS_KEY = "CONNECT_SESSIONS"
    }

    private fun Context.sharedConnectSessionsPreference(): SharedPreferences =
        applicationContext.getSharedPreferences(
            PREFERENCE_FILE_KEY,
            Context.MODE_PRIVATE
        )

    private fun Context.updateSharedConnectSessionsPreference(action: SharedPreferences.Editor.() -> Unit) =
        with(sharedConnectSessionsPreference().edit()) {
            action()
            apply()
        }

    private fun Context.getSharedConnectSessionIds(): Set<String> = sharedConnectSessionsPreference().getStringSet(CONNECT_SESSIONS_KEY, emptySet())!!

    fun getSharedConnectSessions(userIdentifier: String): List<String> {
        val preference = context.sharedConnectSessionsPreference()
        return context.getSharedConnectSessionIds().filter { preference.getString(it, "") == userIdentifier }
    }

    override fun onConnectSessionCreated(userIdentifier: String, connectSessionIdentifier: String) = context.updateSharedConnectSessionsPreference {
        val sessionIds = context.getSharedConnectSessionIds()
        putStringSet(CONNECT_SESSIONS_KEY, sessionIds + connectSessionIdentifier)
        putString(connectSessionIdentifier, userIdentifier)
    }

    override fun onConnectSessionFinished(
        userIdentifier: String,
        connectSessionIdentifier: String
    ) = context.updateSharedConnectSessionsPreference {
        val sessionIds = context.getSharedConnectSessionIds().toMutableSet()
        sessionIds.remove(connectSessionIdentifier)
        putStringSet(CONNECT_SESSIONS_KEY, sessionIds)
        if (context.sharedConnectSessionsPreference().contains(connectSessionIdentifier)) {
            remove(connectSessionIdentifier)
        }
    }
}
