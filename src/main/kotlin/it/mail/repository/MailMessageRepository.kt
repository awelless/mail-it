package it.mail.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import it.mail.domain.MailMessage
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MailMessageRepository: PanacheRepository<MailMessage>