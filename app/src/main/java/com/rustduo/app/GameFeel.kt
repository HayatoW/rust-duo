package com.rustduo.app

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

internal val CorrectTint = Color(0xFFDFF1DA)
internal val IncorrectTint = Color(0xFFFFE5E4)
internal val IncorrectInk = Color(0xFFA72E3A)
private val GoldTint = Color(0xFFFFEBC4)
private val GoldInk = Color(0xFF843B15)

internal fun View.playAnswerHaptic(correct: Boolean) {
    val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (correct) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
    } else {
        if (correct) HapticFeedbackConstants.VIRTUAL_KEY else HapticFeedbackConstants.LONG_PRESS
    }
    performHapticFeedback(effect)
}

@Composable
internal fun Modifier.gameReveal(key: Any): Modifier {
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(progress) { progress.animateTo(1f, tween(210, easing = FastOutSlowInEasing)) }
    return graphicsLayer {
        alpha = 0.7f + 0.3f * progress.value
        translationY = 10.dp.toPx() * (1f - progress.value)
    }
}

internal data class LessonCelebration(
    val streak: Int,
    val advancedStreak: Boolean,
    val unlockedTitle: String?,
)

internal fun lessonCelebration(before: Progress, completed: Progress, next: Lesson?) =
    LessonCelebration(
        streak = completed.streak,
        advancedStreak = before.lastCompleted != completed.lastCompleted,
        unlockedTitle = next?.takeIf { !before.unlocked(it.id) && completed.unlocked(it.id) }?.title,
    )

@Composable
internal fun LessonPathNode(lesson: Lesson, complete: Boolean, unlocked: Boolean, start: () -> Unit) {
    val current = unlocked && !complete
    val face = when {
        complete -> GoldTint
        current -> Green
        else -> Color(0xFFE6EAE2)
    }
    val ink = when {
        complete -> GoldInk
        current -> Color.White
        else -> Color(0xFF687667)
    }
    val state = when {
        complete -> "クリア済み・復習する"
        current -> "次はここ！"
        else -> "前のレッスンで解放"
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (current) {
            Surface(color = CorrectTint, contentColor = Ink, shape = CircleShape) {
                Text(state, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            }
        }
        Surface(
            onClick = start,
            enabled = unlocked,
            modifier = Modifier.padding(top = 6.dp).size(72.dp).testTag("lesson-${lesson.id}")
                .semantics { contentDescription = "${lesson.title}、$state" },
            shape = CircleShape,
            color = face,
            contentColor = ink,
            border = if (current) BorderStroke(3.dp, Color(0xFF135D3D)) else null,
            shadowElevation = if (unlocked) 5.dp else 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(when { complete -> "✓"; current -> "▶"; else -> "🔒" }, fontSize = 27.sp)
            }
        }
        Text(lesson.title, color = if (unlocked) Ink else Color(0xFF687667),
            fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp))
        if (!current) {
            Text(state, color = if (complete) GoldInk else Color(0xFF687667),
                fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
internal fun ResultView(e: LessonEngine?, celebration: LessonCelebration?, failed: Boolean, home: () -> Unit) {
    BackHandler(onBack = home)
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (failed) Text("🌱", fontSize = 78.sp) else VictoryMark()
                Text(if (failed) "今日はここまで" else "レッスンクリア！", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                Text(if (failed) "ハートがなくなりました。\n明日、5 個に回復します。" else e?.lesson?.title.orEmpty(),
                    textAlign = TextAlign.Center, color = Color(0xFF54695B), lineHeight = 26.sp)
                if (!failed && e != null) {
                    Column(Modifier.gameReveal(Unit).padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RewardTile("獲得 XP", "+${e.xp}", GoldTint, GoldInk, Modifier.weight(1f))
                            RewardTile("正答率", "${e.accuracy}%", CorrectTint, Ink, Modifier.weight(1f))
                        }
                        if (celebration != null) {
                            StreakCard(celebration)
                            celebration.unlockedTitle?.let {
                                Text("次のレッスン解放：$it", fontSize = 14.sp, color = Green, fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
        Button(onClick = home, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) {
            Text("学習の道へ", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RewardTile(label: String, value: String, tint: Color, ink: Color, modifier: Modifier = Modifier) {
    Surface(modifier, color = tint, contentColor = ink, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun StreakCard(celebration: LessonCelebration) {
    val title = when {
        !celebration.advancedStreak -> "今日の記録は達成済み"
        celebration.streak == 1 -> "連続学習スタート！"
        else -> "連続記録を更新！"
    }
    Surface(
        Modifier.fillMaxWidth(),
        color = if (celebration.advancedStreak) GoldTint else Color(0xFFEFF1E9),
        contentColor = if (celebration.advancedStreak) GoldInk else Ink,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🔥", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${celebration.streak} 日連続", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun VictoryMark() {
    val burst = remember { Animatable(0f) }
    LaunchedEffect(burst) { burst.animateTo(1f, tween(520, easing = FastOutSlowInEasing)) }
    Box(Modifier.size(width = 232.dp, height = 156.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val progress = burst.value
            val colors = listOf(Color(0xFFD57721), Color(0xFF3A9760), Color(0xFFFFBB49))
            repeat(12) { index ->
                val angle = (index * 30 + 7) * Math.PI / 180
                val spread = 0.6f + 0.4f * progress
                val point = center + Offset(
                    cos(angle).toFloat() * size.width * 0.44f,
                    sin(angle).toFloat() * size.height * 0.44f,
                ) * spread
                val color = colors[index % colors.size]
                val radius = (if (index % 2 == 0) 4.dp else 2.dp).toPx()
                drawCircle(color, radius, point, alpha = progress)
            }
        }
        Surface(
            Modifier.size(112.dp).graphicsLayer {
                val scale = 0.76f + 0.24f * burst.value
                scaleX = scale
                scaleY = scale
            },
            shape = CircleShape, color = GoldTint, border = BorderStroke(3.dp, Color(0xFFFFD98C)),
        ) {
            Box(contentAlignment = Alignment.Center) { Text("🏆", fontSize = 62.sp) }
        }
    }
}
