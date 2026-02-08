package com.vettr.android.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage

/**
 * Verification file for Coil dependency.
 * This file ensures that coil3.compose.AsyncImage can be imported and compiled.
 */
@Composable
fun CoilVerificationComponent(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier
    )
}
