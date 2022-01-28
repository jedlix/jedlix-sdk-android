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

package com.jedlix.sdk.example.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jedlix.sdk.connectSession.registerConnectSessionManager
import com.jedlix.sdk.example.databinding.ActivityConnectBinding
import com.jedlix.sdk.example.viewModel.ConnectViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ConnectActivity : AppCompatActivity() {

    companion object {
        const val USER_IDENTIFIER = "userIdentifier"
    }

    private val viewModel: ConnectViewModel by viewModels {
        ConnectViewModel.Factory(
            intent.extras!!.getString(USER_IDENTIFIER)!!
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityConnectBinding.inflate(LayoutInflater.from(this), null, false)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val connectSessionManager = registerConnectSessionManager {
            viewModel.reloadData()
        }

        lifecycleScope.launch {
            viewModel.didDeauthenticate.collect {
                startActivity(
                    Intent(
                        this@ConnectActivity,
                        AuthenticationActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            }
        }

        lifecycleScope.launch {
            viewModel.didStartConnectSession.collect { (userIdentifier, type) ->
                connectSessionManager.startConnectSession(userIdentifier, type)
            }
        }
    }
}
