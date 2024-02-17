package io.mailit.persistence.postgresql

import io.mailit.template.spi.persistence.TemplateRepository
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Tuple

private const val FIND_BY_MAIL_TYPE_ID_SQL = """
    SELECT mt.mail_message_type_id ${Columns.MailMessageType.ID},
           mt.updated_at ${Columns.MailMessageType.UPDATED_AT},
           t.template ${Columns.MailMessageType.TEMPLATE}
      FROM ${Tables.MAIL_MESSAGE_TYPE} mt
      LEFT JOIN ${Tables.MAIL_MESSAGE_TEMPLATE} t ON mt.mail_message_type_id = t.mail_message_type_id
     WHERE mt.mail_message_type_id = $1
       AND mt.state = 'ENABLED'
"""

class PostgresqlTemplateRepository(
    private val client: PgPool,
) : TemplateRepository {

    override suspend fun findByMailTypeId(mailTypeId: Long) =
        client.preparedQuery(FIND_BY_MAIL_TYPE_ID_SQL)
            .execute(Tuple.of(mailTypeId))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getTemplateFromRow() else null }
            .awaitSuspending()
}
