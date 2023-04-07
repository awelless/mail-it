package io.mailit.core.service.quarkus.id

import io.mailit.core.service.id.InstanceIdProvider
import io.mailit.core.service.id.LeaseLockingInstanceIdProvider
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import javax.enterprise.event.Observes
import kotlinx.coroutines.runBlocking

/**
 * Class that initializes [LeaseLockingInstanceIdProvider] if [InstanceIdProvider] of this type
 */
class LeaseLockingInstanceIdProviderLifecycleManager(
    private val instanceIdProvider: InstanceIdProvider,
) {

    fun onStartup(@Observes event: StartupEvent) {
        if (instanceIdProvider is LeaseLockingInstanceIdProvider) {
            runBlocking { instanceIdProvider.initialize() }
        }
    }

    fun onShutdown(@Observes event: ShutdownEvent) {
        if (instanceIdProvider is LeaseLockingInstanceIdProvider) {
            runBlocking { instanceIdProvider.stop() }
        }
    }
}
