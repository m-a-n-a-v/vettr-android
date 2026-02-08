package com.vettr.android.feature.stockdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vettr.android.core.model.Executive
import com.vettr.android.designsystem.component.SectionHeader
import com.vettr.android.designsystem.theme.Spacing
import com.vettr.android.designsystem.theme.VettrAccent
import com.vettr.android.designsystem.theme.VettrCardBackground
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrTheme
import org.json.JSONArray
import java.util.UUID

/**
 * ExecutiveDetailScreen displays detailed information about an executive
 * in a bottom sheet modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutiveDetailScreen(
    executive: Executive,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        ExecutiveDetailContent(executive = executive)
    }
}

@Composable
fun ExecutiveDetailContent(
    executive: Executive,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(Spacing.lg)
    ) {
        // Header with photo, name, title, tenure
        ExecutiveHeader(executive = executive)

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Career Timeline
        if (executive.previousCompanies.isNotBlank() && executive.previousCompanies != "[]") {
            SectionHeader(title = "Career Timeline")
            Spacer(modifier = Modifier.height(Spacing.sm))
            CareerTimeline(previousCompaniesJson = executive.previousCompanies)
            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        // Education
        SectionHeader(title = "Education")
        Spacer(modifier = Modifier.height(Spacing.sm))
        InfoCard(
            icon = Icons.Default.School,
            text = executive.education
        )
        Spacer(modifier = Modifier.height(Spacing.lg))

        // Specialization
        if (executive.specialization.isNotBlank()) {
            SectionHeader(title = "Specialization")
            Spacer(modifier = Modifier.height(Spacing.sm))
            Box(
                modifier = Modifier
                    .background(VettrAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            ) {
                Text(
                    text = executive.specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VettrAccent,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        // Social Links
        SectionHeader(title = "Connect")
        Spacer(modifier = Modifier.height(Spacing.sm))

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // LinkedIn
            if (!executive.socialLinkedIn.isNullOrBlank()) {
                SocialLinkButton(
                    label = "LinkedIn",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(executive.socialLinkedIn))
                        context.startActivity(intent)
                    }
                )
            }

            // Twitter
            if (!executive.socialTwitter.isNullOrBlank()) {
                SocialLinkButton(
                    label = "Twitter",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(executive.socialTwitter))
                        context.startActivity(intent)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Report button
        SocialLinkButton(
            label = "Report Issue",
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@vettr.ca")
                    putExtra(Intent.EXTRA_SUBJECT, "Report Issue - ${executive.name}")
                }
                context.startActivity(intent)
            },
            icon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

/**
 * Header section with photo, name, title, and tenure.
 */
@Composable
fun ExecutiveHeader(
    executive: Executive,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Photo placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(VettrCardBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Executive photo",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Name, title, tenure
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = executive.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = executive.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Tenure badge
            val tenureColor = if (executive.yearsAtCompany < 1.0) VettrRed else VettrAccent
            Box(
                modifier = Modifier
                    .background(tenureColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = Spacing.sm, vertical = 4.dp)
            ) {
                Text(
                    text = "${String.format("%.1f", executive.yearsAtCompany)} years tenure",
                    style = MaterialTheme.typography.labelMedium,
                    color = tenureColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Career timeline showing previous companies.
 */
@Composable
fun CareerTimeline(
    previousCompaniesJson: String,
    modifier: Modifier = Modifier
) {
    val companies = try {
        val jsonArray = JSONArray(previousCompaniesJson)
        List(jsonArray.length()) { i -> jsonArray.getString(i) }
    } catch (e: Exception) {
        emptyList<String>()
    }

    if (companies.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        companies.take(5).forEach { company ->
            InfoCard(
                icon = Icons.Default.Business,
                text = company
            )
        }
    }
}

/**
 * Info card with icon and text.
 */
@Composable
fun InfoCard(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VettrCardBackground, RoundedCornerShape(8.dp))
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VettrAccent,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Social link button.
 */
@Composable
fun SocialLinkButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .background(VettrCardBackground, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VettrAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = VettrAccent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Previews
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ExecutiveDetailContentPreview() {
    VettrTheme {
        ExecutiveDetailContent(
            executive = Executive(
                id = UUID.randomUUID().toString(),
                stockId = "1",
                name = "John Smith",
                title = "Chief Executive Officer",
                yearsAtCompany = 5.5,
                previousCompanies = """["Microsoft", "Apple", "Google", "Amazon", "Tesla"]""",
                education = "MBA from Harvard Business School, BS in Computer Science from MIT",
                specialization = "Technology & Innovation",
                socialLinkedIn = "https://linkedin.com/in/johnsmith",
                socialTwitter = "https://twitter.com/johnsmith"
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
fun ExecutiveDetailContentRiskPreview() {
    VettrTheme {
        ExecutiveDetailContent(
            executive = Executive(
                id = UUID.randomUUID().toString(),
                stockId = "1",
                name = "Jane Doe",
                title = "Chief Financial Officer",
                yearsAtCompany = 0.8,
                previousCompanies = """["Deloitte", "PwC"]""",
                education = "CPA, Bachelor of Commerce from University of Toronto",
                specialization = "Financial Planning & Analysis",
                socialLinkedIn = null,
                socialTwitter = null
            )
        )
    }
}
