package io.mailit.persistence.h2

import io.mailit.core.model.Slice
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState
import io.mailit.core.spi.DuplicateUniqueKeyException
import io.mailit.core.spi.application.ApplicationRepository
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.h2.Columns.Application as ApplicationCol
import java.sql.SQLException
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner
import org.h2.api.ErrorCode

private const val FIND_BY_ID_SQL = """
    SELECT application_id ${ApplicationCol.ID},
           name ${ApplicationCol.NAME},
           state ${ApplicationCol.STATE}
      FROM application
     WHERE application_id = ?"""

private const val FIND_ALL_SLICED_SQL = """
    SELECT application_id ${ApplicationCol.ID},
           name ${ApplicationCol.NAME},
           state ${ApplicationCol.STATE}
      FROM application
     ORDER BY application_id DESC
     LIMIT ? OFFSET ?"""

private const val INSERT_SQL = """
    INSERT INTO application(
        application_id,
        name,
        state)
    VALUES(?, ?, ?)"""

private const val UPDATE_STATE_SQL = """
    UPDATE application SET state = ?
    WHERE application_id = ?"""

class H2ApplicationRepository(
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
) : ApplicationRepository {

    private val singleMapper = SingleResultSetMapper { it.getApplicationFromRow() }
    private val multipleMapper = MultipleResultSetMapper { it.getApplicationFromRow() }

    override suspend fun findById(id: Long) =
        dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_BY_ID_SQL,
                singleMapper,
                id,
            )
        }

    override suspend fun findAllSliced(page: Int, size: Int): Slice<Application> {
        val offset = page * size

        val content = dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_ALL_SLICED_SQL,
                multipleMapper,
                size + 1,
                offset,
            )
        }

        return createSlice(content, page, size)
    }

    override suspend fun create(application: Application) {
        try {
            dataSource.connection.use {
                queryRunner.update(
                    it,
                    INSERT_SQL,
                    application.id,
                    application.name,
                    application.state.name,
                )
            }
        } catch (e: SQLException) {
            // replace with custom exception handler?
            throw if (e.errorCode == ErrorCode.DUPLICATE_KEY_1) {
                DuplicateUniqueKeyException(e.message, e)
            } else {
                e
            }
        }
    }

    override suspend fun updateState(id: Long, state: ApplicationState) {
        dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_STATE_SQL,
                state.name,
                id,
            )
        }
    }
}
