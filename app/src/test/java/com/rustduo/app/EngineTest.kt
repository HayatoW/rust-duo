package com.rustduo.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class EngineTest {
    private val day = LocalDate.of(2025, 5, 5)
    @Test fun emptyLessonIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { LessonEngine(Lesson(0, "", emptyList()), 5) }
    }
    @Test fun perfectLesson() {
        val e = LessonEngine(Curriculum.lessons.first(), 5)
        repeat(5) { assertTrue(e.check(e.question.answer)); e.advance() }
        assertTrue(e.finished); assertEquals(30, e.xp); assertEquals(100, e.accuracy)
    }
    @Test fun mistakesRetryAtEndAndCostHearts() {
        val e = LessonEngine(Curriculum.lessons.first(), 5)
        val first = e.question
        assertFalse(e.check("wrong")); assertEquals(4, e.hearts); e.advance()
        repeat(4) { e.check(e.question.answer); e.advance() }
        assertEquals(first, e.question)
        e.check(first.answer); e.advance()
        assertTrue(e.finished); assertEquals(83, e.accuracy); assertEquals(28, e.xp)
    }
    @Test fun noHeartsFailsAndNoXp() {
        val e = LessonEngine(Curriculum.lessons.first(), 1)
        e.check(""); assertTrue(e.failed); assertEquals(0, e.xp)
        assertThrows(IllegalStateException::class.java) { e.check("let") }
    }
    @Test fun cannotDoubleCheckOrSkipFeedback() {
        val e = LessonEngine(Curriculum.lessons.first(), 5)
        assertThrows(IllegalStateException::class.java) { e.completeProgress(Progress(), day) }
        assertThrows(IllegalStateException::class.java) { e.advance() }
        e.check("let")
        assertThrows(IllegalStateException::class.java) { e.check("let") }
    }
    @Test fun completionAcrossMidnightKeepsDailyRefill() {
        val e = LessonEngine(Curriculum.lessons.first(), 5)
        var progress = Progress().today(day)
        progress = progress.afterAnswer(e.check("wrong"), day)
        e.advance()
        repeat(5) {
            progress = progress.afterAnswer(e.check(e.question.answer), day)
            e.advance()
        }
        assertEquals(4, e.hearts)
        assertEquals(4, progress.hearts)
        val completed = e.completeProgress(progress, day.plusDays(1))
        assertEquals(5, completed.hearts)
        assertEquals(day.plusDays(1), completed.refillDate)
        assertEquals(day.plusDays(1), completed.lastCompleted)
        assertEquals(setOf(0), completed.completed)
        assertEquals(28, completed.xp)
        assertEquals(1, completed.streak)
    }
    @Test fun answersAfterMidnightOnlySpendTodaysHearts() {
        val yesterday = Progress(hearts = 2, refillDate = day)
        val correct = yesterday.afterAnswer(true, day.plusDays(1))
        assertEquals(5, correct.hearts)
        val wrong = yesterday.afterAnswer(false, day.plusDays(1))
        assertEquals(4, wrong.hearts)
        assertEquals(day.plusDays(1), wrong.refillDate)
        assertEquals(4, wrong.afterAnswer(true, day.plusDays(1)).hearts)
        val secondWrong = wrong.afterAnswer(false, day.plusDays(1))
        assertEquals(3, secondWrong.hearts)
        assertEquals(3, secondWrong.complete(0, 20, day.plusDays(1)).hearts)
    }
    @Test fun sameDayAnswersDoNotRefillOrMakeHeartsNegative() {
        val progress = Progress(hearts = 2, refillDate = day)
        assertEquals(2, progress.afterAnswer(true, day).hearts)
        assertEquals(1, progress.afterAnswer(false, day).hearts)
        assertEquals(0, progress.copy(hearts = 0).afterAnswer(false, day).hearts)
    }
    @Test fun refillAndStreakCalendarBoundaries() {
        val p = Progress(hearts = 1, streak = 3, lastCompleted = day.minusDays(1), refillDate = day.minusDays(1))
        assertEquals(5, p.today(day).hearts)
        val win = p.complete(0, 30, day)
        assertEquals(4, win.streak); assertTrue(win.unlocked(1)); assertFalse(win.unlocked(2))
        assertEquals(4, win.complete(1, 30, day).streak)
        assertEquals(0, win.today(day.plusDays(2)).streak)
        assertEquals(1, win.complete(1, 30, day.plusDays(2)).streak)
        assertEquals(1, p.today(day.minusDays(1)).hearts)
    }
    @Test fun backwardsDateKeepsHeartsAndRefillDate() {
        val p = Progress(hearts = 2, refillDate = day)
        val earlier = p.today(day.minusDays(1))
        assertEquals(2, earlier.hearts)
        assertEquals(day, earlier.refillDate)
        assertEquals(2, earlier.today(day).hearts)
        assertEquals(1, earlier.afterAnswer(false, day.minusDays(1)).hearts)
        assertEquals(5, earlier.today(day.plusDays(1)).hearts)
        assertEquals(5, Progress(hearts = 0).today(day).hearts)
    }
    @Test fun backwardsCompletionKeepsStreakAndLastCompleted() {
        val p = Progress(hearts = 2, streak = 4, lastCompleted = day, refillDate = day)
        val earlier = p.complete(0, 30, day.minusDays(1))
        assertEquals(4, earlier.streak)
        assertEquals(day, earlier.lastCompleted)
        assertEquals(2, earlier.hearts)
        assertEquals(day, earlier.refillDate)
        assertEquals(30, earlier.xp)
        assertTrue(earlier.unlocked(1))
        assertEquals(4, earlier.complete(0, 30, day).streak)
        assertEquals(5, earlier.complete(0, 30, day.plusDays(1)).streak)
        val skipped = earlier.complete(0, 30, day.plusDays(2))
        assertEquals(1, skipped.streak)
        assertEquals(day.plusDays(2), skipped.lastCompleted)
    }
    @Test fun completionKeepsHeartsAlreadyRefilledDuringLesson() {
        val e = LessonEngine(Curriculum.lessons.first(), 2)
        var progress = Progress(hearts = 2, refillDate = day)
        progress = progress.afterAnswer(e.check("wrong"), day.plusDays(1))
        e.advance()
        repeat(5) {
            progress = progress.afterAnswer(e.check(e.question.answer), day.plusDays(1))
            e.advance()
        }
        assertEquals(1, e.hearts)
        val completed = e.completeProgress(progress, day.plusDays(1))
        assertEquals(4, completed.hearts)
        assertEquals(day.plusDays(1), completed.refillDate)
    }
    @Test fun contentIsCompleteAndAnswersAreValid() {
        assertEquals(4, Curriculum.units.size)
        Curriculum.units.forEach { assertEquals(3, it.lessons.size) }
        Curriculum.lessons.forEach { l ->
            assertTrue(l.questions.size >= 5)
            l.questions.forEach { q ->
                assertTrue(q.explanation.isNotBlank())
                if (q.kind == Kind.TILES) assertEquals(q.answer.split(" ").sorted(), q.choices.sorted())
                else { assertEquals(4, q.choices.distinct().size); assertTrue(q.answer in q.choices) }
            }
        }
    }
}
