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

package com.jedlix.sdk.activity.connectSession

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jedlix.sdk.connectSession.ConnectSessionResult
import com.jedlix.sdk.viewModel.connectSession.ConnectSessionArguments
import com.jedlix.sdk.viewModel.connectSession.ConnectSessionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * An [Activity] for handling a [com.jedlix.sdk.model.ConnectSessionDescriptor]
 */
class ConnectSessionActivity : AppCompatActivity() {

    companion object {
        internal const val ARGUMENTS = "arguments"
        private const val loadingIndicatorSize = 48
    }

    internal class Contract : ActivityResultContract<ConnectSessionArguments, ConnectSessionResult>() {
        override fun createIntent(context: Context, input: ConnectSessionArguments): Intent =
            Intent(context, ConnectSessionActivity::class.java).apply {
                putExtra(
                    ARGUMENTS,
                    Json.encodeToString(ConnectSessionArguments.serializer(), input)
                )
            }

        override fun parseResult(resultCode: Int, intent: Intent?): ConnectSessionResult = when (resultCode) {
            RESULT_OK -> ConnectSessionResult.Finished(intent?.getStringExtra(ARGUMENTS) ?: "")
            RESULT_CANCELED -> intent?.getStringExtra(ARGUMENTS)?.let { ConnectSessionResult.InProgress(it) } ?: ConnectSessionResult.NotStarted
            else -> ConnectSessionResult.NotStarted
        }
    }

    /**
     * Used to start a new activity on the browser. The result is [Unit], because no matter what the [androidx.activity.result.ActivityResult] is,
     * the session should be updated.
     */
    internal class NewTabContract : ActivityResultContract<String, Unit>() {
        override fun createIntent(context: Context, input: String): Intent =
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(input)
        }

        override fun parseResult(resultCode: Int, intent: Intent?) = Unit
    }

    private val viewModel by viewModels<ConnectSessionViewModel> {
        ConnectSessionViewModel.Factory(
            Json.decodeFromString(
                ConnectSessionArguments.serializer(),
                intent.extras!!.getString(ARGUMENTS)!!
            )
        )
    }

    private val newTabLauncher = registerForActivityResult(
        NewTabContract(),
        object : ActivityResultCallback<Unit> {
            override fun onActivityResult(result: Unit) {
                viewModel.session.value?.id?.let {
                    viewModel.getConnectSession(it)
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val constraintLayout = RelativeLayout(this)
        setContentView(constraintLayout)
        setUpWebView(constraintLayout)
        setUpLoadingIndicator(constraintLayout)

        lifecycleScope.launch {
            viewModel.onFinished.collect { result ->
                when (result) {
                    is ConnectSessionResult.Finished -> {
                        val intent = Intent().apply {
                            putExtra(ARGUMENTS, result.sessionId)
                        }
                        setResult(Activity.RESULT_OK, intent)
                    }
                    is ConnectSessionResult.InProgress -> {
                        val intent = Intent().apply {
                            putExtra(ARGUMENTS, result.sessionId)
                        }
                        setResult(Activity.RESULT_CANCELED, intent)
                    }
                    is ConnectSessionResult.NotStarted -> {
                        setResult(Activity.RESULT_CANCELED, intent)
                    }
                }
                finish()
            }
        }

        lifecycleScope.launch {
            viewModel.alert.collect { alert ->
                AlertDialog.Builder(this@ConnectSessionActivity).apply {
                    setTitle(alert.title)
                    setMessage(alert.message)
                    alert.positiveButton?.let {
                        setPositiveButton(it.title) { dialog, _ ->
                            it.action()
                            dialog.cancel()
                        }
                    }
                    setNeutralButton(alert.neutralButton.title) { dialog, _ ->
                        alert.neutralButton.action()
                        dialog.cancel()
                    }
                }.show()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        newTabLauncher.unregister()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView(constraintLayout: RelativeLayout) {
        val webView = WebView(this).apply {
            setBackgroundColor(android.graphics.Color.argb(0, 0, 0, 0))
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    val overrideUrlType = viewModel.consumeUrl(url)
                    when (overrideUrlType) {
                        is ConnectSessionViewModel.OverrideUrlTypes.NewTab -> {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(overrideUrlType.url)

                            newTabLauncher.launch(overrideUrlType.url)
                        }
                        is ConnectSessionViewModel.OverrideUrlTypes.Session -> {
                            viewModel.consumeSession(overrideUrlType.session, view, overrideUrlType.url, CookieManager.getInstance())
                        }
                        is ConnectSessionViewModel.OverrideUrlTypes.None -> Unit
                    }
                    return overrideUrlType.shouldOverride()
                }

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                }
            }

            clearCache(true)
            clearHistory()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            lifecycleScope.launch {
                viewModel.webViewUrl.collect { url ->
                    url?.let {
                        loadUrl(it.toString())
                    }
                    visibility = if (url != null) View.VISIBLE else View.GONE
                }
            }
        }
        constraintLayout.addView(webView)
    }

    private fun setUpLoadingIndicator(constraintLayout: RelativeLayout) {
        val indicatorSize = (loadingIndicatorSize * resources.displayMetrics.density).toInt()
        val loadingIndicator = ProgressBar(this).apply {
            layoutParams = RelativeLayout.LayoutParams(indicatorSize, indicatorSize).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            }

            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.isActivityIndicatorVisible.collect {
                    visibility = if (it) View.VISIBLE else View.GONE
                }
            }
        }
        constraintLayout.addView(loadingIndicator)
    }
}
