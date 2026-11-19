package com.rustduo.app

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test

class AccessibilityTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @After fun resetProgress() = runBlocking {
        ProgressStore(InstrumentationRegistry.getInstrumentation().targetContext).save(Progress())
    }

    private fun choice(text: String) =
        compose.onNode(hasText(text, substring = false) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))

    @Test fun lessonControlsHaveSpokenLabels() {
        compose.waitUntil(15000) { compose.onAllNodesWithTag("lesson-0").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("ハート 5 個中 5 個").assertExists()
        compose.onNodeWithTag("lesson-0").performClick()
        compose.onNodeWithText("はじめる").performClick()
        compose.onNodeWithContentDescription("レッスンを閉じる").assertExists()
        compose.onNodeWithContentDescription("ハート 残り 5").assertExists()
        choice("var").performClick()
        choice("var").assertIsSelected()
        choice("let").assertIsNotSelected()
        compose.onNodeWithText("チェック").performClick()
        choice("let").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "正解"))
        choice("var").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "あなたの答え・不正解"))
    }
}
