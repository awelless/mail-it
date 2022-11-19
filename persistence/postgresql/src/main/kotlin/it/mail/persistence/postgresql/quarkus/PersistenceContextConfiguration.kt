package it.mail.persistence.postgresql.quarkus

import io.vertx.mutiny.pgclient.PgPool
import it.mail.persistence.common.id.DistributedIdGenerator
import it.mail.persistence.common.id.IdGenerator
import it.mail.persistence.common.serialization.MailMessageDataSerializer
import it.mail.persistence.postgresql.ReactiveMailMessageRepository
import it.mail.persistence.postgresql.ReactiveMailMessageTypeRepository
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
