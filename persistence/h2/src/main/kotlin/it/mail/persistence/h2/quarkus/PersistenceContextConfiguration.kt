package it.mail.persistence.h2.quarkus

import it.mail.persistence.common.IdGenerator
import it.mail.persistence.common.LocalCounterIdGenerator
import it.mail.persistence.common.serialization.KryoMailMessageDataSerializer
import it.mail.persistence.common.serialization.MailMessageDataSerializer
import it.mail.persistence.h2.JdbcMailMessageRepository
import it.mail.persistence.h2.JdbcMailMessageTypeRepository
import org.apache.commons.dbutils.QueryRunner
import javax.inject.Singleton
import javax.sql.DataSource

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
    private val queryRunner: QueryRunner,
    private val dataSource: DataSource,
) {

    @Singleton
    fun mailMessageTypeRepository() = JdbcMailMessageTypeRepository(idGenerator, dataSource, queryRunner)

    @Singleton
    fun mailMessageRepository(dataSerializer: MailMessageDataSerializer) = JdbcMailMessageRepository(idGenerator, dataSource, queryRunner, dataSerializer)
}
