package com.rustduo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

internal val Green = Color(0xFF218651)
internal val Ink = Color(0xFF203B36)
private val Paper = Color(0xFFF8FAF5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RustDuo(remember { ProgressStore(applicationContext) }) }
    }
}

@Composable
fun RustDuo(store: ProgressStore) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Green, background = Paper, surface = Paper, onSurface = Ink)) {
        val nav = rememberNavController()
        val scope = rememberCoroutineScope()
        val view = LocalView.current
        var progress by remember { mutableStateOf(Progress()) }
        var loaded by remember { mutableStateOf(false) }
        var engine by remember { mutableStateOf<LessonEngine?>(null) }
        var revision by remember { mutableIntStateOf(0) }
        var saving by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf(false) }
        var pendingCompletion by remember { mutableStateOf<Progress?>(null) }
        var celebration by remember { mutableStateOf<LessonCelebration?>(null) }
        fun saveCompletion() {
            val completion = pendingCompletion?.today(LocalDate.now()) ?: return
            if (saving) return
            saving = true
            scope.launch {
                try {
                    store.save(completion)
                    progress = completion
                    pendingCompletion = null
                    nav.navigate("result") { popUpTo("lesson") { inclusive = true } }
                } catch (_: Exception) { error = true }
                finally { saving = false }
            }
        }
        LaunchedEffect(store) {
            progress = store.progress.first().today(LocalDate.now())
            loaded = true
        }
        Surface(Modifier.fillMaxSize()) {
            if (!loaded) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else NavHost(nav, "home", modifier = Modifier.safeDrawingPadding()) {
                composable("home") {
                    Home(progress.today(LocalDate.now())) { lesson ->
                        progress = progress.today(LocalDate.now())
                        pendingCompletion = null
                        celebration = null
                        engine = LessonEngine(lesson, progress.hearts)
                        nav.navigate(if (progress.hearts == 0) "failed" else "lesson")
                    }
                }
                composable("lesson") {
                    val e = engine
                    if (e == null) LaunchedEffect(Unit) { nav.popBackStack("home", false) }
                    else if (e.finished) {
                        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            if (saving) {
                                CircularProgressIndicator()
                                Text("学習結果を保存中…", modifier = Modifier.padding(top = 16.dp))
                            } else {
                                Text("学習結果を保存してください", fontWeight = FontWeight.Bold)
                                Button(onClick = { saveCompletion() }, modifier = Modifier.padding(top = 16.dp)) { Text("保存を再試行") }
                                TextButton(onClick = { nav.popBackStack("home", false) }) { Text("保存せず学習の道へ") }
                            }
                        }
                    } else if (e.failed && e.feedback == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        LessonView(e, saving, revision, onClose = { nav.popBackStack("home", false) }, onCheck = { answer ->
                            if (e.feedback == null && !e.finished && !e.failed) {
                                val correct = e.check(answer)
                                view.playAnswerHaptic(correct)
                                progress = progress.afterAnswer(correct, LocalDate.now())
                                revision++
                                scope.launch { try { store.save(progress) } catch (_: Exception) { error = true } }
                            }
                        }, onNext = {
                            if (e.feedback != null) {
                                e.advance()
                                revision++
                                when {
                                    e.failed -> nav.navigate("failed") { popUpTo("lesson") { inclusive = true } }
                                    e.finished -> {
                                        val completed = e.completeProgress(progress, LocalDate.now())
                                        val next = Curriculum.units.asSequence().flatMap { it.lessons.asSequence() }
                                            .firstOrNull { it.id == e.lesson.id + 1 }
                                        celebration = lessonCelebration(progress, completed, next)
                                        pendingCompletion = completed
                                        saveCompletion()
                                    }
                                }
                            }
                        })
                    }
                }
                composable("result") {
                    ResultView(engine, celebration, false) { nav.popBackStack("home", false) }
                }
                composable("failed") {
                    ResultView(engine, null, true) { nav.popBackStack("home", false) }
                }
            }
            if (error) AlertDialog(onDismissRequest = { error = false }, title = { Text("保存できませんでした") },
                text = { Text("空き容量を確認して、もう一度お試しください。") }, confirmButton = { TextButton(onClick = { error = false }) { Text("閉じる") } })
        }
    }
}

@Composable
private fun Home(p: Progress, start: (Lesson) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("🦀", fontSize = 28.sp)
            Text("🔥 ${p.streak}\n連続日数", color = Color(0xFFB65E18), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("${p.xp} XP\n合計経験値", color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("♥ ${p.hearts}/5\nハート", color = Color(0xFFC64C56), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        HorizontalDivider(color = Color(0xFFE1E8DE))
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(22.dp)) {
            item {
                Text("さあ、Rust を育てよう。", fontWeight = FontWeight.ExtraBold, fontSize = 25.sp)
                Text("1 レッスンずつ、できることを増やそう", color = Color(0xFF708078), modifier = Modifier.padding(top = 6.dp, bottom = 24.dp), fontSize = 13.sp)
            }
            Curriculum.units.forEachIndexed { unitIndex, unit ->
                item {
                    Surface(color = if (unitIndex % 2 == 0) Green else Color(0xFF456C93), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("ユニット ${unitIndex + 1}  /  全 4 ユニット", color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
                            Text(unit.title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, modifier = Modifier.padding(vertical = 4.dp))
                            Text(unit.subtitle, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
                unit.lessons.forEachIndexed { index, lesson ->
                    item {
                        val complete = lesson.id in p.completed
                        val unlocked = p.unlocked(lesson.id)
                        Column(Modifier.fillMaxWidth().padding(top = 12.dp).offset(x = when(index) { 0 -> (-40).dp; 1 -> 40.dp; else -> 0.dp }), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (index > 0) Text("•\n•", color = Color(0xFFCBD6C5), lineHeight = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 8.dp))
                            LessonPathNode(lesson, complete, unlocked) { start(lesson) }
                        }
                    }
                }
            }
            item { Text("毎日の小さな一歩が、確かな力に。", modifier = Modifier.fillMaxWidth().padding(12.dp), textAlign = TextAlign.Center, fontSize = 12.sp) }
        }
        Surface(shadowElevation = 8.dp) { Text("⌂  学習の道", color = Green, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(18.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LessonView(e: LessonEngine, saving: Boolean, revision: Int, onClose: () -> Unit, onCheck: (String) -> Unit, onNext: () -> Unit) {
    var close by remember { mutableStateOf(false) }
    BackHandler { close = true }
    val q = remember(revision) { e.question }
    val feedback = remember(revision) { e.feedback }
    val questionRound = e.attempts - if (feedback != null) 1 else 0
    val progress by animateFloatAsState(
        (e.doneCount + if (feedback == true) 1 else 0).toFloat() / e.lesson.questions.size,
        animationSpec = tween(260),
        label = "lessonProgress",
    )
    val questionScroll = key(questionRound) { rememberScrollState() }
    var choice by remember(q, questionRound) { mutableStateOf("") }
    val selected = remember(q, questionRound) { mutableStateListOf<Int>() }
    val options = remember(q, questionRound) { q.choices.shuffled(kotlin.random.Random(e.lesson.id + questionRound + 19)) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { close = true }) { Text("✕", fontSize = 23.sp, color = Color(0xFF839185)) }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f).height(12.dp), color = Green, trackColor = Color(0xFFE2E9DD), strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
            Text("♥ ${e.hearts}", color = Color(0xFFC64C56), fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        }
        Column(Modifier.weight(1f).gameReveal(questionRound).verticalScroll(questionScroll).padding(horizontal = 24.dp)) {
            Text(if (questionRound >= e.lesson.questions.size) "もう一度チャレンジ" else "${e.lesson.title}  /  ${questionRound + 1} 問目", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 18.dp))
            Text(q.prompt, fontSize = 24.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 20.dp))
            if (q.kind == Kind.CODE) Surface(color = Color(0xFF203B36), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Text(q.code, fontFamily = FontFamily.Monospace, color = Color(0xFFE2F3D8), fontSize = 14.sp, lineHeight = 23.sp, modifier = Modifier.horizontalScroll(rememberScrollState()).padding(18.dp))
            }
            if (q.kind == Kind.TILES) {
                Text("タイルを順番にタップ・戻すときは選んだタイルをタップ", fontSize = 12.sp, color = Color(0xFF708078))
                Surface(shape = RoundedCornerShape(16.dp), color = when (feedback) {
                    true -> CorrectTint
                    false -> IncorrectTint
                    null -> Color(0xFFEAF0E5)
                }, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp).heightIn(min = 96.dp)) {
                    FlowRow(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        selected.forEach { i -> OutlinedButton(enabled = feedback == null, onClick = { selected.remove(i); choice = selected.joinToString(" ") { options[it] } }, contentPadding = PaddingValues(10.dp)) { Text(options[i], fontFamily = FontFamily.Monospace) } }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEachIndexed { i, word -> OutlinedButton(enabled = i !in selected && feedback == null, onClick = { selected.add(i); choice = selected.joinToString(" ") { options[it] } }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(14.dp)) { Text(word, fontFamily = FontFamily.Monospace, fontSize = 17.sp) } }
                }
            } else options.forEachIndexed { index, answer ->
                val active = choice == answer
                val correctAnswer = feedback != null && answer == q.answer
                val wrongChoice = feedback == false && active
                val accent = when {
                    wrongChoice -> IncorrectInk
                    correctAnswer || active -> Green
                    else -> Color(0xFFD5DFD0)
                }
                val tint = when {
                    wrongChoice -> IncorrectTint
                    correctAnswer -> CorrectTint
                    active -> Color(0xFFE4F3E4)
                    else -> Color.White
                }
                Surface(shape = RoundedCornerShape(16.dp), border = BorderStroke(if (active || correctAnswer) 2.dp else 1.dp, accent), color = tint,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable(enabled = feedback == null) { choice = answer }) {
                    Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(when { correctAnswer -> "✓"; wrongChoice -> "✕"; else -> "${index + 1}" },
                            fontSize = 12.sp, color = if (active || correctAnswer) accent else Color(0xFF74836F),
                            modifier = Modifier.padding(end = 18.dp))
                        Text(answer, fontSize = 17.sp, fontWeight = if (active || correctAnswer) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        Surface(color = when (feedback) { true -> CorrectTint; false -> IncorrectTint; null -> Paper }) {
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                if (feedback != null) {
                    val reveal = remember(questionRound) { MutableTransitionState(false).apply { targetState = true } }
                    val accent = if (feedback) Green else IncorrectInk
                    AnimatedVisibility(visibleState = reveal,
                        enter = expandVertically(expandFrom = Alignment.Bottom, animationSpec = tween(180)) + fadeIn(tween(160)),
                        exit = ExitTransition.None) {
                        Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(shape = CircleShape, color = accent, contentColor = Color.White) {
                                    Box(Modifier.size(32.dp).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
                                        Text(if (feedback) "✓" else "✕", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(if (feedback) "正解！" else "不正解", fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = accent)
                            }
                            if (!feedback) Text("正しい答え：${q.answer}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            Text(q.explanation, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                        }
                    }
                }
                Button(onClick = { if (feedback == null) onCheck(choice) else onNext() }, enabled = !saving && (feedback != null || choice.isNotEmpty()),
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (feedback == false) IncorrectInk else Green)) {
                    Text(if (saving) "保存中…" else if (feedback == null) "チェック" else "続ける", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
    if (close) AlertDialog(onDismissRequest = { close = false }, title = { Text("学習を中断しますか？") }, text = { Text("このレッスンの進み具合は保存されません。減ったハートはそのままです。") }, confirmButton = { TextButton(onClick = onClose) { Text("中断する") } }, dismissButton = { TextButton(onClick = { close = false }) { Text("学習に戻る") } })
}
