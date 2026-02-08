package com.vettr.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VettrTheme {
                VettrNavHost(
                    modifier = Modifier.fillMaxSize(),
                    authRepository = authRepository
                )
            }
        }
    }
}

