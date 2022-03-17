package it.mail.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@Transactional
@ApplicationScoped
class MailMessageRepository : PanacheRepository<MailMessage> {

    fun findOneWithTypeByIdAndStatus(id: Long, statuses: Collection<MailMessageStatus>): MailMessage? {
        return find("id = ?1 AND status IN ?2", id, statuses)
            .withFetchGraph("MailMessage[type]")
            .firstResult()
    }

    fun findOneByExternalId(externalId: String): MailMessage? =
        find("externalId", externalId)
            .firstResult()

    fun findAllIdsByStatusIn(statuses: Collection<MailMessageStatus>): List<Long> =
        find("status IN ?1", statuses)
            .project(Long::class.java)
            .list()
}
