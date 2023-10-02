package io.mailit.persistence.h2.quarkus

import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.h2.H2ApiKeyRepository
import io.mailit.persistence.h2.H2MailMessageRepository
import io.mailit.persistence.h2.H2MailMessageTypeRepository
import jakarta.inject.Singleton
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
    fun apiKeyRepository() = H2ApiKeyRepository(dataSource, queryRunner)

    @Singleton
    fun mailMessageTypeRepository() = H2MailMessageTypeRepository(dataSource, queryRunner)

    @Singleton
    fun mailMessageRepository(dataSerializer: MailMessageDataSerializer) = H2MailMessageRepository(dataSource, queryRunner, dataSerializer)
}
