package com.vettr.android.feature.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrNavy
import com.vettr.android.designsystem.theme.VettrTextPrimary
import com.vettr.android.designsystem.theme.VettrTextSecondary
import com.vettr.android.designsystem.theme.VettrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contact & Support",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VettrTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VettrTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VettrNavy
                )
            )
        },
        containerColor = VettrNavy,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            // Email Support Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = VettrCardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = VettrAccent,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Text(
                        text = "Email Support",
                        style = MaterialTheme.typography.titleMedium,
                        color = VettrTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = "We typically respond within 24 hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = VettrTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@vettr.ca")
                                putExtra(Intent.EXTRA_SUBJECT, "VETTR Support Request")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send email"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VettrAccent
                        )
                    ) {
                        Text(
                            text = "support@vettr.ca",
                            fontWeight = FontWeight.SemiBold,
                            color = VettrNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Feedback Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = VettrCardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Send Feedback",
                        style = MaterialTheme.typography.titleMedium,
                        color = VettrTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = "Help us improve VETTR with your suggestions",
                        style = MaterialTheme.typography.bodySmall,
                        color = VettrTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@vettr.ca")
                                putExtra(Intent.EXTRA_SUBJECT, "VETTR Feedback")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send feedback"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Send Feedback Email",
                            fontWeight = FontWeight.SemiBold,
                            color = VettrAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Common Topics
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = VettrCardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md)
                ) {
                    Text(
                        text = "Common Topics",
                        style = MaterialTheme.typography.titleMedium,
                        color = VettrTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    ContactTopic(
                        title = "Account & Billing",
                        description = "Subscription changes, account deletion, data requests"
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    ContactTopic(
                        title = "Bug Reports",
                        description = "Report issues with the app or data"
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    ContactTopic(
                        title = "Feature Requests",
                        description = "Suggest new features or improvements"
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun ContactTopic(
    title: String,
    description: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = VettrTextPrimary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = VettrTextSecondary
        )
    }
}

@Preview
@Composable
private fun ContactScreenPreview() {
    VettrTheme {
        ContactScreen()
    }
}
