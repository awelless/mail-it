package io.mailit.persistence.postgresql

import io.mailit.core.model.Slice
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState
import io.mailit.core.spi.DuplicateUniqueKeyException
import io.mailit.core.spi.application.ApplicationRepository
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.postgresql.Columns.Application as ApplicationCol
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Tuple
import io.vertx.pgclient.PgException

private const val FIND_BY_ID_SQL = """
    SELECT application_id ${ApplicationCol.ID},
           name ${ApplicationCol.NAME},
           state ${ApplicationCol.STATE}
      FROM application
     WHERE application_id = $1"""

private const val FIND_ALL_SLICED_SQL = """
    SELECT application_id ${ApplicationCol.ID},
           name ${ApplicationCol.NAME},
           state ${ApplicationCol.STATE}
      FROM application
     ORDER BY application_id DESC
     LIMIT $1 OFFSET $2"""

private const val INSERT_SQL = """
    INSERT INTO application(
        application_id,
        name,
        state)
    VALUES($1, $2, $3)"""

private const val UPDATE_STATE_SQL = """
    UPDATE application SET state = $1
    WHERE application_id = $2"""

class PostgresqlApplicationRepository(
    private val client: PgPool,
) : ApplicationRepository {

    override suspend fun findById(id: Long) =
        client.preparedQuery(FIND_BY_ID_SQL).execute(Tuple.of(id))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getApplicationFromRow() else null }
            .awaitSuspending()

    override suspend fun findAllSliced(page: Int, size: Int): Slice<Application> {
        val offset = page * size

        return client.preparedQuery(FIND_ALL_SLICED_SQL).execute(Tuple.of(size + 1, offset))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getApplicationFromRow() }
            .collect().asList()
            .onItem().transform { createSlice(it, page, size) }
            .awaitSuspending()
    }

    override suspend fun create(application: Application) {
        client.preparedQuery(INSERT_SQL).execute(Tuple.of(application.id, application.name, application.state.name))
            .onFailure(PgException::class.java).transform {
                if ((it as? PgException)?.sqlState == "23505") DuplicateUniqueKeyException(it.message, it) else it
            }
            .awaitSuspending()
    }

    override suspend fun updateState(id: Long, state: ApplicationState) {
        client.preparedQuery(UPDATE_STATE_SQL).execute(Tuple.of(state.name, id))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()
    }
}
