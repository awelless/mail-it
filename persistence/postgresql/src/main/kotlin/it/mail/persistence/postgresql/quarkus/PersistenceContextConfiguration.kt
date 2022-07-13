package it.mail.persistence.postgresql.quarkus

import io.vertx.mutiny.pgclient.PgPool
import it.mail.persistence.common.IdGenerator
import it.mail.persistence.common.LocalCounterIdGenerator
import it.mail.persistence.common.serialization.KryoMailMessageDataSerializer
import it.mail.persistence.common.serialization.MailMessageDataSerializer
import it.mail.persistence.postgresql.ReactiveMailMessageRepository
import it.mail.persistence.postgresql.ReactiveMailMessageTypeRepository
import org.apache.commons.dbutils.QueryRunner
import javax.inject.Singleton

class PersistenceContextConfiguration {

    @Singleton
    fun idGenerator() = LocalCounterIdGenerator()

    @Singleton
    fun kryoMailMessageDataSerializer() = KryoMailMessageDataSerializer()

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
