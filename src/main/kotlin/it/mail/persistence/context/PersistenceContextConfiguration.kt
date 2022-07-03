package it.mail.persistence.context

import io.vertx.mutiny.pgclient.PgPool
import it.mail.persistence.common.IdGenerator
import it.mail.persistence.common.LocalCounterIdGenerator
import it.mail.persistence.common.serialization.KryoMailMessageDataSerializer
import it.mail.persistence.common.serialization.MailMessageDataSerializer
import it.mail.persistence.context.DatabaseType.H2
import it.mail.persistence.context.DatabaseType.POSTGRESQL
import it.mail.persistence.jdbc.JdbcMailMessageRepository
import it.mail.persistence.jdbc.JdbcMailMessageTypeRepository
import it.mail.persistence.reactive.ReactiveMailMessageTypeRepository
import it.mail.persistence.reactive.postgresql.ReactiveMailMessageRepository
import org.apache.commons.dbutils.QueryRunner
import javax.inject.Provider
import javax.inject.Singleton
import javax.sql.DataSource

internal class PersistenceContextConfiguration {

    @Singleton
    internal fun idGenerator() = LocalCounterIdGenerator()

    @Singleton
    internal fun kryoMailMessageDataSerializer() = KryoMailMessageDataSerializer()

    @Singleton
    internal fun queryRunner() = QueryRunner()
}

internal class RepositoriesConfiguration(
    private val idGenerator: IdGenerator,
    private val queryRunner: QueryRunner,
    private val dataSource: Provider<DataSource>,
    private val client: Provider<PgPool>,
    private val databaseConfig: DatabaseConfig,
) {

    @Singleton
    internal fun mailMessageTypeRepository() =
        when (databaseConfig.type()) {
            H2 -> JdbcMailMessageTypeRepository(idGenerator, dataSource.get(), queryRunner)
            POSTGRESQL -> ReactiveMailMessageTypeRepository(idGenerator, client.get())
        }

    @Singleton
    internal fun mailMessageRepository(dataSerializer: MailMessageDataSerializer) =
        when (databaseConfig.type()) {
            H2 -> JdbcMailMessageRepository(idGenerator, dataSource.get(), queryRunner, dataSerializer)
            POSTGRESQL -> ReactiveMailMessageRepository(idGenerator, client.get(), dataSerializer)
        }
}
