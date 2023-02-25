package io.mailit.persistence.postgresql.quarkus

import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.postgresql.ReactiveMailMessageRepository
import io.mailit.persistence.postgresql.ReactiveMailMessageTypeRepository
import io.vertx.mutiny.pgclient.PgPool
import javax.inject.Singleton
import org.apache.commons.dbutils.QueryRunner

class PersistenceContextConfiguration {

    @Singleton
    fun queryRunner() = QueryRunner()
}

class RepositoriesConfiguration(
    private val pgPool: PgPool,
) {

    @Singleton
    fun mailMessageTypeRepository() = ReactiveMailMessageTypeRepository(pgPool)

    @Singleton
    fun mailMessageRepository(dataSerializer: MailMessageDataSerializer) = ReactiveMailMessageRepository(pgPool, dataSerializer)
}
