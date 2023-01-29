package jp.kawagh.kiando

import android.webkit.WebView
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
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
    ) { padding ->
        AndroidView(factory = ::WebView, modifier = Modifier.padding(padding)) {
            with(it) {
                loadUrl("file:///android_asset/licenses.html")
            }
        }
    }
}