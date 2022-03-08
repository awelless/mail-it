package it.mail.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import it.mail.domain.MailMessage
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MailMessageRepository : PanacheRepository<MailMessage> {

    fun findOneByExternalId(externalId: String): MailMessage? {
        val query = find(
            """
            SELECT m FROM MailMessage m 
             WHERE m.externalId = ?1""",
            externalId)

        return query.firstResult()
    }
}