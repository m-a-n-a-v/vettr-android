package com.vettr.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.feature.main.VettrNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    private var deepLinkUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle deep link from intent
        handleDeepLink(intent)

        setContent {
            VettrTheme {
                VettrNavHost(
                    modifier = Modifier.fillMaxSize(),
                    authRepository = authRepository,
                    deepLinkUri = deepLinkUri,
                    onDeepLinkHandled = { deepLinkUri = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                // Handle vettr:// or https://vettr.app deep links
                deepLinkUri = intent.data
            }
        }
    }
}

