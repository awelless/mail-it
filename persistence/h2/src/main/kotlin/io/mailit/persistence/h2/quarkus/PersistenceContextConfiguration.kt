package io.mailit.persistence.h2.quarkus

import io.mailit.persistence.common.id.IdGenerator
import io.mailit.persistence.common.id.InMemoryIdGenerator
import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.h2.JdbcMailMessageRepository
import io.mailit.persistence.h2.JdbcMailMessageTypeRepository
import javax.inject.Singleton
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner

class PersistenceContextConfiguration {

    @Singleton
    fun idGenerator() = InMemoryIdGenerator()

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
