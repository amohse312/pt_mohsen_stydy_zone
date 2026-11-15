package com.ptstudyzone.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ptstudyzone.app.databinding.ActivityMainBinding

/**
 * PT Study Zone — WebView wrapper for https://ptmohsen.free.nf/
 *
 * The website itself is the app: the same pages, login and progress.
 * Progress is preserved exactly like in the browser, because WebView keeps
 * the site's localStorage / IndexedDB / cookies / cache on the phone
 * (app-private storage, survives closing the app and rebooting).
 * Nothing is cleared on exit, so when the user is back online the site
 * talks to its server and syncs exactly as it does on the web.
 * Offline, the WebView first tries its cache before showing the offline screen.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        const val HOME_URL = "https://ptmohsen.free.nf/"
        const val SITE_HOST = "ptmohsen.free.nf"
    }

    private lateinit var binding: ActivityMainBinding
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private var restoredState = false

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = fileUploadCallback
        fileUploadCallback = null
        if (callback == null) return@registerForActivityResult

        if (result.resultCode != RESULT_OK || result.data == null) {
            callback.onReceiveValue(null)
            return@registerForActivityResult
        }

        val data = result.data!!
        val uris = mutableListOf<Uri>()
        data.clipData?.let { clip ->
            for (i in 0 until clip.itemCount) {
                uris.add(clip.getItemAt(i).uri)
            }
        }
        data.data?.let { uris.add(it) }
        callback.onReceiveValue(uris.toTypedArray())
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupWebView()
        setupRefresh()
        setupOfflineRetry()
        setupBackPress()

        // Restore the exact page + scroll position after rotation / process death.
        if (savedInstanceState != null) {
            restoredState = binding.webView.restoreState(savedInstanceState) != null
        }
        if (!restoredState) {
            val startUrl = intent?.data?.toString()?.takeIf { it.contains(SITE_HOST) } ?: HOME_URL
            loadUrl(startUrl)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.toString()?.takeIf { it.contains(SITE_HOST) }?.let { loadUrl(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::binding.isInitialized) binding.webView.saveState(outState)
    }

    /** Keep the progress bar below the status bar (edge-to-edge). */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.progressBar) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = view.layoutParams as android.widget.FrameLayout.LayoutParams
            params.topMargin = bars.top
            view.layoutParams = params
            insets
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webView = binding.webView

        // Login + progress persistence: cookies and DOM storage are ON and
        // are never cleared by this app.
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true // localStorage / sessionStorage — where progress lives
            databaseEnabled = true // IndexedDB / Web SQL
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            builtInZoomControls = false
            displayZoomControls = false
            // Responsive: fit the site's mobile layout on phones, tablets, foldables.
            useWideViewPort = true
            loadWithOverviewMode = true
            textZoom = 100
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
            // Identify the app to the server without breaking site detection.
            userAgentString = "$userAgentString PTStudyZoneApp/1.0"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleUrl(url)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return url?.let { handleUrl(it) } ?: false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                binding.offlineLayout.visibility = View.GONE
                binding.webView.visibility = View.VISIBLE
                // Persist cookies immediately so login survives a force-close.
                CookieManager.getInstance().flush()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                // Only for the main page, not a sub-resource (image/CSS).
                if (request?.isForMainFrame == true) {
                    binding.swipeRefresh.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    showOffline()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                binding.progressBar.progress = newProgress
                if (newProgress == 100) binding.swipeRefresh.isRefreshing = false
            }

            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = callback
                return try {
                    val intent = params?.createIntent()
                        ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                        }
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    fileUploadCallback = null
                    Toast.makeText(this@MainActivity, R.string.no_file_app, Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            downloadFile(url, userAgent, contentDisposition, mimeType)
        }
    }

    /**
     * Keep site links inside the app. Open phone, mail, WhatsApp/Telegram
     * and other external links in their own apps / the browser.
     */
    private fun handleUrl(url: String): Boolean {
        val uri = Uri.parse(url)
        val host = uri.host ?: ""

        return when {
            url.startsWith("tel:") || url.startsWith("mailto:") ||
                url.startsWith("whatsapp:") || url.startsWith("tg:") ||
                url.startsWith("intent:") -> {
                openExternally(uri)
                true
            }
            host == SITE_HOST || host.endsWith(".$SITE_HOST") -> false // load in WebView
            else -> {
                openExternally(uri)
                true
            }
        }
    }

    private fun openExternally(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app_to_open, Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadFile(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String
    ) {
        try {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                addRequestHeader(
                    "Cookie",
                    CookieManager.getInstance().getCookie(url) ?: ""
                )
                setTitle(fileName)
                setDescription(getString(R.string.downloading))
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                allowScanningByMediaScanner()
            }
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(this, getString(R.string.download_started, fileName), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.webView.reload()
        }
    }

    private fun setupOfflineRetry() {
        binding.retryButton.setOnClickListener {
            if (isOnline()) {
                binding.offlineLayout.visibility = View.GONE
                binding.webView.visibility = View.VISIBLE
                binding.webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
                binding.webView.reload()
            } else {
                // Still offline: retry from cache, the site may already be cached.
                binding.webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                binding.webView.reload()
                Toast.makeText(this, R.string.still_offline, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun loadUrl(url: String) {
        // Offline: serve the cached site first instead of failing immediately.
        binding.webView.settings.cacheMode = if (isOnline()) {
            WebSettings.LOAD_DEFAULT
        } else {
            WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
        binding.webView.loadUrl(url)
    }

    private fun showOffline() {
        binding.webView.visibility = View.GONE
        binding.offlineLayout.visibility = View.VISIBLE
    }

    private fun isOnline(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
        // Flush cookies + storage so progress/login are written to disk now.
        CookieManager.getInstance().flush()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onDestroy() {
        // Do NOT clear cache, history, cookies or WebStorage here:
        // that is what keeps login and progress on the phone.
        binding.webView.destroy()
        super.onDestroy()
    }
}
