package io.mailit.persistence.h2

import io.mailit.template.spi.persistence.TemplateRepository
import io.mailit.value.MailTypeId
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner

private const val FIND_BY_MAIL_TYPE_ID_SQL = """
    SELECT mt.mail_message_type_id ${Columns.MailMessageType.ID},
           mt.updated_at ${Columns.MailMessageType.UPDATED_AT},
           t.template ${Columns.MailMessageType.TEMPLATE}
      FROM ${Tables.MAIL_MESSAGE_TYPE} mt
      LEFT JOIN ${Tables.MAIL_MESSAGE_TEMPLATE} t ON mt.mail_message_type_id = t.mail_message_type_id
     WHERE mt.mail_message_type_id = ?
       AND mt.state = 'ENABLED'
"""

class H2TemplateRepository(
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
) : TemplateRepository {

    private val singleMapper = SingleResultSetMapper { it.getTemplateFromRow() }

    override suspend fun findByMailTypeId(mailTypeId: MailTypeId) = dataSource.connection.use {
        queryRunner.query(
            it,
            FIND_BY_MAIL_TYPE_ID_SQL,
            singleMapper,
            mailTypeId.value,
        )
    }
}
