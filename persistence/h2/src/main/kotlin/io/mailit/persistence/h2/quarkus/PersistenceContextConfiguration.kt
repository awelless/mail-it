package io.mailit.persistence.h2.quarkus

import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.h2.JdbcMailMessageRepository
import io.mailit.persistence.h2.JdbcMailMessageTypeRepository
import javax.inject.Singleton
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner

class PersistenceContextConfiguration {

    @Singleton
    fun queryRunner() = QueryRunner()
}

class RepositoriesConfiguration(
    private val queryRunner: QueryRunner,
    private val dataSource: DataSource,
) {

    @Singleton
    fun mailMessageTypeRepository() = JdbcMailMessageTypeRepository(dataSource, queryRunner)

    @Singleton
    fun mailMessageRepository(dataSerializer: MailMessageDataSerializer) = JdbcMailMessageRepository(dataSource, queryRunner, dataSerializer)
}
