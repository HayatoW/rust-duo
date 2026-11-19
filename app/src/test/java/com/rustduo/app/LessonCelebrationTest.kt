package com.rustduo.app

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class LessonCelebrationTest {
    private val day = LocalDate.of(2026, 1, 3)
    private val next = Lesson(1, "可変性", emptyList())

    @Test fun firstCompletionCelebratesDayAndNewLesson() {
        val before = Progress()
        val after = before.complete(0, 30, day)

        assertEquals(LessonCelebration(1, true, "可変性"), lessonCelebration(before, after, next))
    }

    @Test fun sameDayReplayDoesNotRepeatCelebrations() {
        val before = Progress().complete(0, 30, day)
        val after = before.complete(0, 30, day)

        assertEquals(LessonCelebration(1, false, null), lessonCelebration(before, after, next))
    }

    @Test fun nextDayReplayCelebratesOnlyStreak() {
        val before = Progress().complete(0, 30, day.minusDays(1))
        val after = before.complete(0, 30, day)

        assertEquals(LessonCelebration(2, true, null), lessonCelebration(before, after, next))
    }

    @Test fun finalLessonDoesNotPromiseAnotherUnlock() {
        val before = Progress().complete(0, 30, day)
        val after = before.complete(1, 30, day)

        assertEquals(LessonCelebration(1, false, null), lessonCelebration(before, after, next = null))
    }
}
