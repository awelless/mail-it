package io.mailit.persistence.postgresql.quarkus

import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.postgresql.PostgresqlInstanceIdLocks
import io.mailit.persistence.postgresql.ReactiveMailMessageRepository
import io.mailit.persistence.postgresql.ReactiveMailMessageTypeRepository
import io.vertx.mutiny.pgclient.PgPool
import jakarta.inject.Singleton

class RepositoriesConfiguration(
    private val pgPool: PgPool,
) {

    @Singleton
    fun mailMessageTypeRepository() = ReactiveMailMessageTypeRepository(pgPool)

    @Singleton
    fun mailMessageRepository(dataSerializer: MailMessageDataSerializer) = ReactiveMailMessageRepository(pgPool, dataSerializer)

    @Singleton
    fun instanceIdLocks() = PostgresqlInstanceIdLocks(pgPool)
}
