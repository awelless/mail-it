package it.mail.persistence.context

import it.mail.persistence.common.IdGenerator
import it.mail.persistence.common.LocalCounterIdGenerator
import it.mail.persistence.jdbc.JdbcMailMessageRepository
import it.mail.persistence.jdbc.JdbcMailMessageTypeRepository
import org.apache.commons.dbutils.QueryRunner
import javax.enterprise.context.ApplicationScoped
import javax.sql.DataSource

class PersistenceContextConfiguration {

    @ApplicationScoped
    fun queryRunner() = QueryRunner()

    @ApplicationScoped
    fun idGenerator() = LocalCounterIdGenerator()
}

@ApplicationScoped
class RepositoriesConfiguration(
    private val idGenerator: IdGenerator,
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
) {

    @ApplicationScoped
    fun mailMessageTypeRepository() = JdbcMailMessageTypeRepository(idGenerator, dataSource, queryRunner)

    @ApplicationScoped
    fun mailMessageRepository() = JdbcMailMessageRepository(idGenerator, dataSource, queryRunner)
}
