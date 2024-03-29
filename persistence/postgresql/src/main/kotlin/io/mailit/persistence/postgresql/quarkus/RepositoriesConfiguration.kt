package io.mailit.persistence.postgresql.quarkus

import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.postgresql.PostgresqlApiKeyRepository
import io.mailit.persistence.postgresql.PostgresqlMailMessageRepository
import io.mailit.persistence.postgresql.PostgresqlMailMessageTypeRepository
import io.mailit.persistence.postgresql.PostgresqlServerLeaseLocks
import io.mailit.persistence.postgresql.PostgresqlTemplateRepository
import io.vertx.mutiny.pgclient.PgPool
import jakarta.inject.Singleton

class RepositoriesConfiguration(
    private val pgPool: PgPool,
) {

    @Singleton
    fun apiKeyRepository() = PostgresqlApiKeyRepository(pgPool)

    @Singleton
    fun mailMessageTypeRepository() = PostgresqlMailMessageTypeRepository(pgPool)

    @Singleton
    fun mailMessageRepository(dataSerializer: MailMessageDataSerializer) = PostgresqlMailMessageRepository(pgPool, dataSerializer)

    @Singleton
    fun templateRepository() = PostgresqlTemplateRepository(pgPool)

    @Singleton
    fun instanceIdLocks() = PostgresqlServerLeaseLocks(pgPool)
}
