package com.vettr.android.core.model

/**
 * Represents different types of alerts that can be triggered in the VETTR platform.
 */
enum class AlertType(
    val displayName: String,
    val iconName: String
) {
    FINANCING(
        displayName = "Financing",
        iconName = "account_balance"
    ),
    CONSOLIDATION(
        displayName = "Consolidation",
        iconName = "merge_type"
    ),
    DRILL_RESULTS(
        displayName = "Drill Results",
        iconName = "construction"
    ),
    MANAGEMENT_CHANGE(
        displayName = "Management Change",
        iconName = "swap_horiz"
    ),
    RED_FLAG(
        displayName = "Red Flag",
        iconName = "flag"
    )
}
