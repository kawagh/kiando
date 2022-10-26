package jp.kawagh.kiando

import android.webkit.WebView
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun LicenseScreen(onArrowBackPressed: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "license") },
                navigationIcon = {
                    IconButton(onClick = onArrowBackPressed) {
                        Icon(Icons.Default.ArrowBack, "back to list")
                    }
                })
        }
    ) {
        AndroidView(factory = ::WebView) {
            with(it) {
                loadUrl("file:///android_asset/licenses.html")
            }
        }
    }
}
