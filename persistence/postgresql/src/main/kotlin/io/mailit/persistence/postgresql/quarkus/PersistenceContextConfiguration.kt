package io.mailit.persistence.postgresql.quarkus

import io.mailit.persistence.common.id.DistributedIdGenerator
import io.mailit.persistence.common.id.IdGenerator
import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.postgresql.ReactiveMailMessageRepository
import io.mailit.persistence.postgresql.ReactiveMailMessageTypeRepository
import io.vertx.mutiny.pgclient.PgPool
import javax.inject.Singleton
import org.apache.commons.dbutils.QueryRunner

class PersistenceContextConfiguration {

    // instanceIdProvider is constant
    // should be replaced with a real implementation to scale horizontally
    @Singleton
    fun idGenerator() = DistributedIdGenerator { 1 }

    @Singleton
    fun queryRunner() = QueryRunner()
}

class RepositoriesConfiguration(
    private val idGenerator: IdGenerator,
    private val pgPool: PgPool,
) {

    @Singleton
    fun mailMessageTypeRepository() = ReactiveMailMessageTypeRepository(idGenerator, pgPool)

    @Singleton
    fun mailMessageRepository(dataSerializer: MailMessageDataSerializer) = ReactiveMailMessageRepository(idGenerator, pgPool, dataSerializer)
}
