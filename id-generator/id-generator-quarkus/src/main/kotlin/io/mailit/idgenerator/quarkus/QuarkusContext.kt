package io.mailit.idgenerator.quarkus

import io.mailit.idgenerator.context.IdGeneratorContext
import io.mailit.idgenerator.spi.locking.ServerLeaseLocks
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.event.Observes
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking

class QuarkusContext(
    serverLeaseLocks: ServerLeaseLocks,
) {
    private val idGeneratorContext = IdGeneratorContext.create(serverLeaseLocks)

    @Singleton
    fun idGenerator() = idGeneratorContext.idGenerator

    @Suppress("UNUSED_PARAMETER")
    fun onStartup(@Observes event: StartupEvent) = runBlocking { idGeneratorContext.onStartup() }

    @Suppress("UNUSED_PARAMETER")
    fun onShutdown(@Observes event: ShutdownEvent) = runBlocking { idGeneratorContext.onShutdown() }
}
