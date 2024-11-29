//package app.smarthomeapp
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//
//class CameraFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        Log.d("CameraFragment", "onCreateView called")
//        return inflater.inflate(R.layout.fragment_camera, container, false)
//
//        //
//    }
//}


package app.smarthomeapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class CameraFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("CameraFragment", "onCreateView called")

        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        val webView: WebView = view.findViewById(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        // Set WebViewClient to handle page navigation within the WebView
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        // Load stream URL
        webView.loadUrl("https://rt.ivs.rocks/demos/pk-mode")

        return view
    }
}
