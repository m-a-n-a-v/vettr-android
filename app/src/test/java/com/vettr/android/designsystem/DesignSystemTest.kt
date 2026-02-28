package com.vettr.android.designsystem

import androidx.compose.ui.graphics.Color
import com.vettr.android.designsystem.component.getChangeColor
import com.vettr.android.designsystem.component.getScoreColor
import com.vettr.android.designsystem.component.getScoreLabel
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for design system components.
 * Tests the business logic of VettrScoreView and MetricCard components.
 * Updated for VETR Score V2 5-tier scale (90/75/50/30 thresholds).
 */
class DesignSystemTest {

    // V2 Score Color Constants
    private val darkGreen = Color(0xFF198754)   // Exceptional (>= 90)
    private val limeGreen = Color(0xFF84CC16)   // Healthy (>= 75)
    private val yellow = Color(0xFFFBBF24)      // Neutral (>= 50)
    private val orange = Color(0xFFF97316)      // High Risk (>= 30)
    private val deepRed = Color(0xFFDC2626)     // Toxic (< 30)

    // VettrScoreView Color Logic Tests - V2 Exceptional Tier (>= 90)

    @Test
    fun `getScoreColor returns dark green for score 95`() {
        val color = getScoreColor(95)
        assertEquals(darkGreen, color)
    }

    @Test
    fun `getScoreColor returns dark green for score 90`() {
        val color = getScoreColor(90)
        assertEquals(darkGreen, color)
    }

    @Test
    fun `getScoreColor returns dark green for score 100`() {
        val color = getScoreColor(100)
        assertEquals(darkGreen, color)
    }

    // VettrScoreView Color Logic Tests - V2 Healthy Tier (>= 75, < 90)

    @Test
    fun `getScoreColor returns lime green for score 89`() {
        val color = getScoreColor(89)
        assertEquals(limeGreen, color)
    }

    @Test
    fun `getScoreColor returns lime green for score 80`() {
        val color = getScoreColor(80)
        assertEquals(limeGreen, color)
    }

    @Test
    fun `getScoreColor returns lime green for score 75`() {
        val color = getScoreColor(75)
        assertEquals(limeGreen, color)
    }

    // VettrScoreView Color Logic Tests - V2 Neutral Tier (>= 50, < 75)

    @Test
    fun `getScoreColor returns yellow for score 74`() {
        val color = getScoreColor(74)
        assertEquals(yellow, color)
    }

    @Test
    fun `getScoreColor returns yellow for score 60`() {
        val color = getScoreColor(60)
        assertEquals(yellow, color)
    }

    @Test
    fun `getScoreColor returns yellow for score 50`() {
        val color = getScoreColor(50)
        assertEquals(yellow, color)
    }

    // VettrScoreView Color Logic Tests - V2 High Risk Tier (>= 30, < 50)

    @Test
    fun `getScoreColor returns orange for score 49`() {
        val color = getScoreColor(49)
        assertEquals(orange, color)
    }

    @Test
    fun `getScoreColor returns orange for score 40`() {
        val color = getScoreColor(40)
        assertEquals(orange, color)
    }

    @Test
    fun `getScoreColor returns orange for score 30`() {
        val color = getScoreColor(30)
        assertEquals(orange, color)
    }

    // VettrScoreView Color Logic Tests - V2 Toxic Tier (< 30)

    @Test
    fun `getScoreColor returns deep red for score 29`() {
        val color = getScoreColor(29)
        assertEquals(deepRed, color)
    }

    @Test
    fun `getScoreColor returns deep red for score 15`() {
        val color = getScoreColor(15)
        assertEquals(deepRed, color)
    }

    @Test
    fun `getScoreColor returns deep red for score 0`() {
        val color = getScoreColor(0)
        assertEquals(deepRed, color)
    }

    @Test
    fun `getScoreColor normalizes out-of-range scores`() {
        // Test upper bound
        val colorAbove100 = getScoreColor(150)
        assertEquals(darkGreen, colorAbove100) // 100 maps to dark green (Exceptional)

        // Test lower bound
        val colorBelow0 = getScoreColor(-10)
        assertEquals(deepRed, colorBelow0) // 0 maps to deep red (Toxic)
    }

    // VettrScoreView Label Logic Tests - V2 Tier Names

    @Test
    fun `getScoreLabel returns Exceptional for score 95`() {
        val label = getScoreLabel(95)
        assertEquals("Exceptional", label)
    }

    @Test
    fun `getScoreLabel returns Exceptional for score 90`() {
        val label = getScoreLabel(90)
        assertEquals("Exceptional", label)
    }

    @Test
    fun `getScoreLabel returns Healthy for score 89`() {
        val label = getScoreLabel(89)
        assertEquals("Healthy", label)
    }

    @Test
    fun `getScoreLabel returns Healthy for score 80`() {
        val label = getScoreLabel(80)
        assertEquals("Healthy", label)
    }

    @Test
    fun `getScoreLabel returns Healthy for score 75`() {
        val label = getScoreLabel(75)
        assertEquals("Healthy", label)
    }

    @Test
    fun `getScoreLabel returns Neutral for score 74`() {
        val label = getScoreLabel(74)
        assertEquals("Neutral", label)
    }

    @Test
    fun `getScoreLabel returns Neutral for score 60`() {
        val label = getScoreLabel(60)
        assertEquals("Neutral", label)
    }

    @Test
    fun `getScoreLabel returns Neutral for score 50`() {
        val label = getScoreLabel(50)
        assertEquals("Neutral", label)
    }

    @Test
    fun `getScoreLabel returns High Risk for score 49`() {
        val label = getScoreLabel(49)
        assertEquals("High Risk", label)
    }

    @Test
    fun `getScoreLabel returns High Risk for score 30`() {
        val label = getScoreLabel(30)
        assertEquals("High Risk", label)
    }

    @Test
    fun `getScoreLabel returns Toxic for score 29`() {
        val label = getScoreLabel(29)
        assertEquals("Toxic", label)
    }

    @Test
    fun `getScoreLabel returns Toxic for score 0`() {
        val label = getScoreLabel(0)
        assertEquals("Toxic", label)
    }

    // MetricCard Change Indicator Tests

    @Test
    fun `getChangeColor returns green for positive change`() {
        val color = getChangeColor(5.25)
        assertEquals(VettrGreen, color)
    }

    @Test
    fun `getChangeColor returns red for negative change`() {
        val color = getChangeColor(-2.15)
        assertEquals(VettrRed, color)
    }

    @Test
    fun `getChangeColor returns green for zero change`() {
        val color = getChangeColor(0.0)
        assertEquals(VettrGreen, color)
    }

    @Test
    fun `getChangeColor returns green for small positive change`() {
        val color = getChangeColor(0.01)
        assertEquals(VettrGreen, color)
    }

    @Test
    fun `getChangeColor returns red for small negative change`() {
        val color = getChangeColor(-0.01)
        assertEquals(VettrRed, color)
    }
}
