package it.mail.persistence.h2

import io.quarkus.test.junit.QuarkusTest
import it.mail.persistence.test.MailMessageRepositoryTest

@QuarkusTest
class H2MailMessageRepositoryTest : MailMessageRepositoryTest()
