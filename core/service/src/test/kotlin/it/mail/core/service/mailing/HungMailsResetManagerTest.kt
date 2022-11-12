package it.mail.core.service.mailing

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageType
import it.mail.test.createMailMessage
import it.mail.test.createPlainMailMessageType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class HungMailsResetManagerTest {

    private val batchSize = 200

    @RelaxedMockK
    lateinit var mailMessageService: MailMessageService

    @InjectMockKs
    lateinit var hungMailsResetManager: HungMailsResetManager

    lateinit var type: MailMessageType
    lateinit var mail1: MailMessage
    lateinit var mail2: MailMessage

    @BeforeEach
    fun setUp() {
        type = createPlainMailMessageType()
        mail1 = createMailMessage(type)
        mail2 = createMailMessage(type)
    }

    @Test
    fun resetAllHungMails_considersThemAsFailed() = runTest {
        // given
        coEvery { mailMessageService.getHungMessages(batchSize) }.returns(listOf(mail1, mail2))

        // when
        hungMailsResetManager.resetAllHungMails()

        // then
        coVerify { mailMessageService.processFailedDelivery(mail1) }
        coVerify { mailMessageService.processFailedDelivery(mail2) }
    }

    @Test
    fun resetAllHungMails_many_processesInBatches() = runTest {
        // given
        val manyMails = List(batchSize) { createMailMessage(type) }

        coEvery { mailMessageService.getHungMessages(batchSize) } returns manyMails andThen listOf(mail1, mail2)

        // when
        hungMailsResetManager.resetAllHungMails()

        // then
        coVerify(exactly = 202) { mailMessageService.processFailedDelivery(any()) }
        coVerify(exactly = 1) { mailMessageService.processFailedDelivery(mail1) }
        coVerify(exactly = 1) { mailMessageService.processFailedDelivery(mail2) }
    }
}
