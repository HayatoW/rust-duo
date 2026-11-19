package com.rustduo.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class LessonIntroTest {
    @get:Rule val compose = createComposeRule()

    @Test fun noHeartsDisablesStartUntilNextDayRefill() {
        val day = LocalDate.of(2025, 5, 5)
        val today = mutableStateOf(day)
        val progress = Progress(hearts = 0, refillDate = day)
        var starts = 0
        compose.setContent {
            MaterialTheme {
                LessonIntroView(Curriculum.lessons.first(), progress.today(today.value).hearts, onClose = {}) { starts++ }
            }
        }
        compose.onNodeWithText("はじめる").assertIsNotEnabled().performClick()
        compose.onNodeWithText("ハートがありません。明日、5 個に回復します。").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, starts); today.value = day.plusDays(1) }
        compose.onNodeWithText("はじめる").assertIsEnabled().performClick()
        compose.onNodeWithText("ハートがありません。明日、5 個に回復します。").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, starts) }
    }
}
