package com.vettr.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.vettr.android.core.data.repository.AuthRepository
import com.vettr.android.core.util.BiometricCheckResult
import com.vettr.android.core.util.BiometricService
import com.vettr.android.designsystem.theme.VettrTheme
import com.vettr.android.feature.main.VettrNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var biometricService: BiometricService

    private var deepLinkUri by mutableStateOf<Uri?>(null)
    private var isAppUnlocked by mutableStateOf(false)
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle deep link from intent
        handleDeepLink(intent)

        // Start unlocked initially (biometric prompt will show on resume if needed)
        isAppUnlocked = true

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            VettrTheme {
                VettrNavHost(
                    modifier = Modifier.fillMaxSize(),
                    authRepository = authRepository,
                    deepLinkUri = deepLinkUri,
                    onDeepLinkHandled = { deepLinkUri = null },
                    windowSizeClass = windowSizeClass
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Skip biometric prompt on first resume (onCreate)
        if (isFirstResume) {
            isFirstResume = false
            return
        }

        // Check if biometric login is enabled and show prompt
        lifecycleScope.launch {
            val biometricEnabled = biometricService.isBiometricLoginEnabled()
            val biometricAvailable = biometricService.isBiometricAvailable() is BiometricCheckResult.Available

            if (biometricEnabled && biometricAvailable && !isAppUnlocked) {
                showBiometricPrompt()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Lock the app when going to background
        isAppUnlocked = false
    }

    private fun showBiometricPrompt() {
        biometricService.showBiometricPrompt(
            activity = this,
            onSuccess = {
                isAppUnlocked = true
            },
            onError = { errorMessage ->
                // Error is shown by BiometricPrompt
                // User can try again or use password fallback
            },
            onFallbackToPassword = {
                // For now, just unlock the app (password auth would be implemented in a future story)
                isAppUnlocked = true
            }
        )
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
