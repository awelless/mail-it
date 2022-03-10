package it.mail.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import it.mail.domain.MailMessageType
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@Transactional
@ApplicationScoped
class MailMessageTypeRepository : PanacheRepository<MailMessageType> {

    fun findOneByName(name: String): MailMessageType? {
        val query = find(
            """
            SELECT t FROM MailMessageType t 
             WHERE t.name = ?1""",
            name)

        return query.firstResult()
    }
}
