package io.mailit.persistence.mysql.quarkus

import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.mysql.MysqlApiKeyRepository
import io.mailit.persistence.mysql.MysqlMailMessageRepository
import io.mailit.persistence.mysql.MysqlMailMessageTypeRepository
import io.mailit.persistence.mysql.MysqlServerLeaseLocks
import io.mailit.persistence.mysql.MysqlTemplateRepository
import io.vertx.mutiny.mysqlclient.MySQLPool
import jakarta.inject.Singleton

class RepositoriesConfiguration(
    private val mysqlPool: MySQLPool,
) {

    @Singleton
    fun apiKeyRepository() = MysqlApiKeyRepository(mysqlPool)

    @Singleton
    fun mailMessageTypeRepository() = MysqlMailMessageTypeRepository(mysqlPool)

    @Singleton
    fun mailMessageRepository(dataSerializer: MailMessageDataSerializer) = MysqlMailMessageRepository(mysqlPool, dataSerializer)

    @Singleton
    fun templateRepository() = MysqlTemplateRepository(mysqlPool)

    @Singleton
    fun instanceIdLocks() = MysqlServerLeaseLocks(mysqlPool)
}
