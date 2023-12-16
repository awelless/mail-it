package io.mailit.idgenerator

import io.mailit.idgenerator.api.IdGenerator
import io.mailit.idgenerator.context.IdGeneratorContext
import io.mailit.idgenerator.fake.InMemoryServerLeaseLocks
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ForkJoinPool
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IgGeneratorTest {

    private lateinit var context: IdGeneratorContext
    private lateinit var idGenerator: IdGenerator

    @BeforeEach
    fun setUp() {
        context = IdGeneratorContext.create(InMemoryServerLeaseLocks())

        idGenerator = context.idGenerator
        runBlocking { context.onStartup() }
    }

    @AfterEach
    fun tearDown() {
        runBlocking { context.onShutdown() }
    }

    @Test
    fun generateId_highConcurrency() {
        // given
        val generations = 100_000

        val pool = ForkJoinPool.commonPool()
        val countdown = CountDownLatch(1)

        // when
        val tasks = (1..generations)
            .map {
                pool.submit(
                    Callable {
                        countdown.await()
                        idGenerator.generateId()
                    },
                )
            }

        countdown.countDown()

        val generatedIdsCount = tasks
            .map { it.get() }
            .distinct()
            .count()

        // then
        assertEquals(generations, generatedIdsCount)
    }
}
