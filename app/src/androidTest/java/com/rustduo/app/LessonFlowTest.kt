package com.rustduo.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class LessonFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun completeLessonWithMistakeAndRetry() {
        compose.waitUntil(15000) { compose.onAllNodesWithTag("lesson-0").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("lesson-0").performClick()
        compose.onNodeWithText("このレッスンで学ぶこと").assertIsDisplayed()
        compose.onNodeWithText("変数を宣言するキーワードは？").assertDoesNotExist()
        compose.onNodeWithText("✕").performClick()
        compose.onNodeWithText("♥ 5/5\nハート").assertIsDisplayed()
        compose.onNodeWithTag("lesson-0").performClick()
        compose.onNodeWithText("このレッスンで学ぶこと").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("0 XP\n合計経験値").assertIsDisplayed()
        compose.onNodeWithTag("lesson-0").performClick()
        compose.onNodeWithText("はじめる").performClick()
        compose.onNodeWithText("var").performClick()
        compose.onNodeWithText("チェック").performClick()
        compose.onNodeWithText("不正解").assertIsDisplayed()
        compose.onNodeWithText("続ける").performClick()
        compose.onNodeWithText("5", useUnmergedTree = false).performClick()
        checkAndContinue()
        listOf("let", "mut", "x", "=", "5;").forEach { compose.onNodeWithText(it, substring = false).performClick() }
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
        compose.onNodeWithTag("lesson-1").assertExists()
    }
    private fun checkAndContinue() {
        compose.onNodeWithText("チェック").performClick()
        compose.onNodeWithText("正解！").assertIsDisplayed()
        compose.onNodeWithText("続ける").performClick()
    }
}
