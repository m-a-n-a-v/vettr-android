package com.vettr.android.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Dimensions
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrTheme

/**
 * Apply standard card styling to a Modifier.
 * Includes background color, rounded corners, and elevation shadow.
 *
 * @param elevation Shadow elevation for the card (default 2.dp)
 * @return Modified Modifier with card styling
 */
fun Modifier.cardStyle(elevation: Dp = 2.dp): Modifier = this
    .shadow(
        elevation = elevation,
        shape = RoundedCornerShape(Dimensions.cardRadius)
    )
    .clip(RoundedCornerShape(Dimensions.cardRadius))
    .background(VettrCardBackground)

/**
 * Apply standard VETTR padding to a Modifier.
 *
 * @param padding Padding value (default is Spacing.md = 16.dp)
 * @return Modified Modifier with padding
 */
fun Modifier.vettrPadding(padding: Dp = Spacing.md): Modifier = this
    .padding(padding)

/**
 * Preview demonstrating the card style modifier extension.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun CardStylePreview() {
    VettrTheme {
        Box(
            modifier = Modifier
                .cardStyle()
                .vettrPadding()
        ) {
            Text(text = "Sample Card Content")
        }
    }
}

/**
 * Preview demonstrating the vettrPadding modifier extension with different values.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun VettrPaddingPreview() {
    VettrTheme {
        Box(
            modifier = Modifier
                .cardStyle()
                .vettrPadding(Spacing.lg)
        ) {
            Text(text = "Large Padding Example")
        }
    }
}
