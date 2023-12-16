package io.mailit.idgenerator.context

import io.mailit.idgenerator.api.IdGenerator
import io.mailit.idgenerator.core.DistributedIdGenerator
import io.mailit.idgenerator.core.LeaseLockingServerIdProvider
import io.mailit.idgenerator.spi.locking.ServerLeaseLocks
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers

class IdGeneratorContext private constructor(
    val idGenerator: IdGenerator,
    val onStartup: suspend () -> Unit,
    val onShutdown: suspend () -> Unit,
) {
    companion object {
        fun create(serverLeaseLocks: ServerLeaseLocks): IdGeneratorContext {
            val serverIdProvider = LeaseLockingServerIdProvider(
                serverLeaseLocks = serverLeaseLocks,
                lockProlongationCoroutineContext = Dispatchers.Default,
                lockDuration = 15.minutes,
                prolongationDelay = 30.seconds,
            )

            val idGenerator = DistributedIdGenerator(serverIdProvider)

            return IdGeneratorContext(
                idGenerator,
                serverIdProvider::initialize,
                serverIdProvider::stop,
            )
        }
    }
}
