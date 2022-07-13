package it.mail.persistence.postgresql

import io.quarkus.test.junit.QuarkusTest
import it.mail.persistence.test.MailMessageRepositoryTest

@QuarkusTest
class PostgresqlMailMessageRepositoryTest : MailMessageRepositoryTest()
