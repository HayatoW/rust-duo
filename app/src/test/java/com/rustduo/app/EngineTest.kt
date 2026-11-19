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
        assertThrows(IllegalStateException::class.java) { e.advance() }
        e.check("let")
        assertThrows(IllegalStateException::class.java) { e.check("let") }
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
