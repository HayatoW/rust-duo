package com.rustduo.app

import java.time.LocalDate

enum class Kind { CHOICE, CODE, TILES }
data class Question(val prompt: String, val choices: List<String>, val answer: String, val explanation: String, val kind: Kind = Kind.CHOICE, val code: String = "")
data class Lesson(val id: Int, val title: String, val questions: List<Question>)
data class UnitContent(val title: String, val subtitle: String, val lessons: List<Lesson>)
data class Progress(val completed: Set<Int> = emptySet(), val xp: Int = 0, val hearts: Int = 5, val streak: Int = 0, val lastCompleted: LocalDate? = null, val refillDate: LocalDate? = null) {
    fun today(date: LocalDate): Progress = copy(
        hearts = if (refillDate != date) 5 else hearts,
        refillDate = date,
        streak = if (lastCompleted != null && lastCompleted < date.minusDays(1)) 0 else streak
    )
    fun unlocked(id: Int) = id == 0 || id - 1 in completed
    fun afterAnswer(correct: Boolean, date: LocalDate): Progress {
        val current = today(date)
        return current.copy(hearts = (current.hearts - if (correct) 0 else 1).coerceAtLeast(0))
    }
    fun complete(id: Int, earned: Int, date: LocalDate): Progress {
        val current = today(date)
        return current.copy(completed = completed + id, xp = xp + earned,
            streak = if (lastCompleted == date) current.streak else if (lastCompleted == date.minusDays(1)) current.streak + 1 else 1,
            lastCompleted = date)
    }
}
class LessonEngine(val lesson: Lesson, initialHearts: Int) {
    init { require(lesson.questions.isNotEmpty()) }
    private val queue = ArrayDeque(lesson.questions.indices.toList())
    var hearts = initialHearts.coerceIn(0, 5); private set
    var attempts = 0; private set
    var correct = 0; private set
    var feedback: Boolean? = null; private set
    val failed get() = hearts == 0
    val finished get() = queue.isEmpty() && !failed
    val question get() = lesson.questions[queue.first()]
    val doneCount get() = lesson.questions.size - queue.size.coerceAtMost(lesson.questions.size)
    val accuracy get() = if (attempts == 0) 0 else correct * 100 / attempts
    val xp get() = if (finished) 20 + accuracy / 10 else 0
    fun completeProgress(progress: Progress, date: LocalDate): Progress {
        check(finished)
        return progress.complete(lesson.id, xp, date)
    }
    fun check(answer: String): Boolean {
        check(!failed && !finished && feedback == null)
        attempts++
        val ok = answer.trim() == question.answer
        if (ok) correct++ else hearts--
        feedback = ok
        return ok
    }
    fun advance() {
        check(feedback != null)
        val index = queue.removeFirst()
        if (feedback == false && !failed) queue.addLast(index)
        feedback = null
    }
}
