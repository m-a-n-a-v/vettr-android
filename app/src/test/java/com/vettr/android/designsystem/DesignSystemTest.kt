package com.vettr.android.designsystem

import androidx.compose.ui.graphics.Color
import com.vettr.android.designsystem.component.getChangeColor
import com.vettr.android.designsystem.component.getScoreColor
import com.vettr.android.designsystem.component.getScoreLabel
import com.vettr.android.designsystem.theme.VettrGreen
import com.vettr.android.designsystem.theme.VettrRed
import com.vettr.android.designsystem.theme.VettrYellow
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for design system components.
 * Tests the business logic of VettrScoreView and MetricCard components.
 */
class DesignSystemTest {

    // VettrScoreView Color Logic Tests

    @Test
    fun `getScoreColor returns green for score 85`() {
        val color = getScoreColor(85)
        assertEquals(VettrGreen, color)
    }

    @Test
    fun `getScoreColor returns green for score 81`() {
        val color = getScoreColor(81)
        assertEquals(VettrGreen, color)
    }

    @Test
    fun `getScoreColor returns yellow for score 80`() {
        val color = getScoreColor(80)
        assertEquals(VettrYellow, color)
    }

    @Test
    fun `getScoreColor returns yellow for score 70`() {
        val color = getScoreColor(70)
        assertEquals(VettrYellow, color)
    }

    @Test
    fun `getScoreColor returns yellow for score 60`() {
        val color = getScoreColor(60)
        assertEquals(VettrYellow, color)
    }

    @Test
    fun `getScoreColor returns orange for score 59`() {
        val color = getScoreColor(59)
        assertEquals(Color(0xFFFF9800), color)
    }

    @Test
    fun `getScoreColor returns orange for score 50`() {
        val color = getScoreColor(50)
        assertEquals(Color(0xFFFF9800), color)
    }

    @Test
    fun `getScoreColor returns orange for score 40`() {
        val color = getScoreColor(40)
        assertEquals(Color(0xFFFF9800), color)
    }

    @Test
    fun `getScoreColor returns red for score 39`() {
        val color = getScoreColor(39)
        assertEquals(VettrRed, color)
    }

    @Test
    fun `getScoreColor returns red for score 30`() {
        val color = getScoreColor(30)
        assertEquals(VettrRed, color)
    }

    @Test
    fun `getScoreColor returns red for score 0`() {
        val color = getScoreColor(0)
        assertEquals(VettrRed, color)
    }

    @Test
    fun `getScoreColor normalizes out-of-range scores`() {
        // Test upper bound
        val colorAbove100 = getScoreColor(150)
        assertEquals(VettrGreen, colorAbove100) // 100+ maps to green

        // Test lower bound
        val colorBelow0 = getScoreColor(-10)
        assertEquals(VettrRed, colorBelow0) // <0 maps to red
    }

    // VettrScoreView Label Logic Tests

    @Test
    fun `getScoreLabel returns Strong Buy for score 85`() {
        val label = getScoreLabel(85)
        assertEquals("Strong Buy", label)
    }

    @Test
    fun `getScoreLabel returns Strong Buy for score 81`() {
        val label = getScoreLabel(81)
        assertEquals("Strong Buy", label)
    }

    @Test
    fun `getScoreLabel returns Buy for score 80`() {
        val label = getScoreLabel(80)
        assertEquals("Buy", label)
    }

    @Test
    fun `getScoreLabel returns Buy for score 70`() {
        val label = getScoreLabel(70)
        assertEquals("Buy", label)
    }

    @Test
    fun `getScoreLabel returns Buy for score 60`() {
        val label = getScoreLabel(60)
        assertEquals("Buy", label)
    }

    @Test
    fun `getScoreLabel returns Hold for score 59`() {
        val label = getScoreLabel(59)
        assertEquals("Hold", label)
    }

    @Test
    fun `getScoreLabel returns Hold for score 50`() {
        val label = getScoreLabel(50)
        assertEquals("Hold", label)
    }

    @Test
    fun `getScoreLabel returns Hold for score 40`() {
        val label = getScoreLabel(40)
        assertEquals("Hold", label)
    }

    @Test
    fun `getScoreLabel returns Caution for score 39`() {
        val label = getScoreLabel(39)
        assertEquals("Caution", label)
    }

    @Test
    fun `getScoreLabel returns Caution for score 30`() {
        val label = getScoreLabel(30)
        assertEquals("Caution", label)
    }

    @Test
    fun `getScoreLabel returns Caution for score 0`() {
        val label = getScoreLabel(0)
        assertEquals("Caution", label)
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
