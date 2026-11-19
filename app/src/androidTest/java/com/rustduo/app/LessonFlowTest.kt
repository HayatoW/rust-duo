package com.rustduo.app

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class LessonFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun completeLessonWithMistakeAndRetry() {
        compose.waitUntil(15000) { compose.onAllNodesWithTag("lesson-0").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("次はここ！").assertIsDisplayed()
        compose.onNodeWithTag("lesson-0").performClick()
        compose.onNodeWithText("var").performClick()
        compose.onNodeWithText("チェック").performClick()
        compose.onNodeWithText("不正解").assertIsDisplayed()
        compose.onNodeWithText("正しい答え：let").assertIsDisplayed()
        assertProgress(0f)
        compose.onNodeWithText("続ける").performClick()
        compose.onNodeWithText("5", useUnmergedTree = false).performClick()
        compose.onNodeWithText("チェック").performClick()
        compose.onNodeWithText("正解！").assertIsDisplayed()
        assertProgress(0.2f)
        compose.onNodeWithText("続ける").performClick()
        listOf("let", "n", "=", "8;").forEach { compose.onNodeWithText(it, substring = false).performClick() }
        checkAndContinue()
        compose.onNodeWithText("変更不可").performClick()
        checkAndContinue()
        compose.onNodeWithText(";", substring = false).performClick()
        checkAndContinue()
        compose.onNodeWithText("let", substring = false).performClick()
        checkAndContinue()
        compose.waitUntil(10000) { compose.onAllNodesWithText("レッスンクリア！").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("83%").assertIsDisplayed()
        compose.onNodeWithText("+28").assertIsDisplayed()
        compose.onNodeWithText("学習の道へ").performClick()
        compose.onNodeWithTag("lesson-1").assertIsEnabled()
        compose.onNodeWithText("クリア済み・復習する").assertIsDisplayed()
        compose.onNodeWithText("次はここ！").assertIsDisplayed()
    }

    private fun assertProgress(value: Float) {
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertRangeInfoEquals(ProgressBarRangeInfo(value, 0f..1f))
    }

    private fun checkAndContinue() {
        compose.onNodeWithText("チェック").performClick()
        compose.onNodeWithText("正解！").assertIsDisplayed()
        compose.onNodeWithText("続ける").performClick()
    }
}
