package com.example.android_only_webview_padding_poc

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import com.example.android_only_webview_padding_poc.ui.theme.AndroidonlywebviewpaddingpocTheme

class MainActivity : ComponentActivity() {

    val testWithIframe = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        class MyClient : WebViewClientCompat() {
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceErrorCompat
            ) {
                super.onReceivedError(view, request, error)
                Log.d("JS CALLBACK ERROR", "${error.errorCode}, ${error.description}")
            }
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val req = super.shouldInterceptRequest(view, request)
                req?.apply {
                    req.responseHeaders["Access-Control-Allow-Origin"] = "*";
                }
                return req
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val jsString = if (testWithIframe) {
                    "document.getElementById('wrapper-iframe').contentWindow.window.document.documentElement.style.setProperty('--isAndroid', 1)"
                } else {
                    "document.documentElement.style.setProperty('--isAndroid', 1)"
                }
                view?.evaluateJavascript(jsString) {
                    Log.d("JS CALLBACK EVAL", it)
                }
            }
        }

        class MyChromeClient : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                val temp = super.onConsoleMessage(consoleMessage)
                Log.d("JS CALLBACK CONSOLE", "${consoleMessage?.message()}")
                return temp
            }
        }


        setContent {
            AndroidonlywebviewpaddingpocTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AndroidView(
                        factory = { context ->
                            val webView = WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                webViewClient = MyClient()
                                settings.javaScriptEnabled = true

                                // This only works because I'm using a file URL :(
                                // No work around - CORS must be disabled for this to work with iframes
                                // settings.allowFileAccessFromFileURLs = testWithIframe
                                webChromeClient = MyChromeClient()
                            }
                            return@AndroidView webView
                        },
                        update = { webView ->
                            if (testWithIframe) {
                                webView.loadUrl("file:///android_asset/exampleWrapper.html")
                            } else {
                                webView.loadUrl("file:///android_asset/example.html")
                            }

                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
