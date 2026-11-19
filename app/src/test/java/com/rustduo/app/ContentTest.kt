package com.rustduo.app

import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.IOException
import java.nio.file.Files

class ContentTest {
    private val questions = Curriculum.lessons.flatMap { it.questions }

    @Test fun curriculumHasTwelveFiveQuestionLessons() {
        assertEquals(60, questions.size)
        assertEquals(12, Curriculum.lessons.size)
        assertEquals((0..11).toList(), Curriculum.lessons.map { it.id })
        Curriculum.lessons.forEach { assertEquals(5, it.questions.size) }
    }

    @Test fun choicesAndExplanationsAreValid() {
        questions.filter { it.kind == Kind.CHOICE || it.kind == Kind.CODE }.forEach { q ->
            assertEquals(q.prompt, 4, q.choices.size)
            assertEquals(q.prompt, 4, q.choices.distinct().size)
            assertTrue(q.prompt, q.answer in q.choices)
            assertTrue(q.prompt, q.explanation.isNotBlank())
        }
    }

    @Test fun tilesCanFormTheExactAnswer() {
        questions.filter { it.kind == Kind.TILES }.forEach { q ->
            assertTrue(q.prompt, q.choices.all { it.isNotEmpty() && ' ' !in it })
            assertEquals(q.prompt, q.answer.split(" ").sorted(), q.choices.sorted())
        }
    }

    @Test fun codeAnswersMatchRustc() {
        val rustcAvailable = try {
            ProcessBuilder("rustc", "--version").start().let { process ->
                process.inputStream.bufferedReader().use { it.readText() }
                process.waitFor() == 0
            }
        } catch (_: IOException) {
            false
        }
        assumeTrue("rustc must be available on PATH", rustcAvailable)
        val codeQuestions = questions.filter { it.kind == Kind.CODE }
        assertFalse(codeQuestions.isEmpty())
        codeQuestions.forEachIndexed { index, q ->
            val directory = Files.createTempDirectory("rustduo-code-$index").toFile()
            try {
                val source = directory.resolve("main.rs").apply { writeText(q.code) }
                val executable = directory.resolve("main")
                val compiler = ProcessBuilder("rustc", "--edition", "2021", source.absolutePath,
                    "-o", executable.absolutePath).redirectErrorStream(true).start()
                val diagnostics = compiler.inputStream.bufferedReader().use { it.readText() }
                val compileExit = compiler.waitFor()
                if (q.answer == "エラー") {
                    assertNotEquals("CODE $index should fail compilation", 0, compileExit)
                } else {
                    assertEquals("CODE $index: $diagnostics", 0, compileExit)
                    val process = ProcessBuilder(executable.absolutePath)
                        .redirectError(ProcessBuilder.Redirect.INHERIT).start()
                    val output = process.inputStream.bufferedReader().use { it.readText() }
                    assertEquals("CODE $index should run successfully", 0, process.waitFor())
                    assertEquals("CODE $index output", q.answer, output.trim())
                }
            } finally {
                directory.deleteRecursively()
            }
        }
    }
}
