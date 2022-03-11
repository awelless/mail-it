package it.mail.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import it.mail.domain.MailMessage
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@Transactional
@ApplicationScoped
class MailMessageRepository : PanacheRepository<MailMessage> {

    fun findOneByExternalId(externalId: String): MailMessage? =
            find("externalId", externalId)
                    .firstResult()
}
