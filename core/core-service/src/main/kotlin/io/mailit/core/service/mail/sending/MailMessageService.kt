package io.mailit.core.service.mail.sending

import io.mailit.core.model.MailMessage
import io.mailit.core.spi.MailMessageRepository
import io.mailit.value.MailId
import io.mailit.value.MailState.CANCELED
import io.mailit.value.MailState.FAILED
import io.mailit.value.MailState.PENDING
import io.mailit.value.MailState.RETRY
import io.mailit.value.MailState.SENDING
import io.mailit.value.MailState.SENT
import io.mailit.value.exception.NotFoundException
import java.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import mu.KLogging

class MailMessageService(
    private val mailMessageRepository: MailMessageRepository,
    private val clock: Clock,
) {
    companion object : KLogging()

    private val hungMessageStates = listOf(SENDING)
    private val hungMessageDuration = 2.minutes.toJavaDuration()

    private val possibleToSendMessageStates = listOf(PENDING, RETRY)

    suspend fun getHungMessages(maxListSize: Int) =
        mailMessageRepository.findAllWithTypeByStatesAndSendingStartedBefore(hungMessageStates, clock.instant().minus(hungMessageDuration), maxListSize)

    suspend fun getAllIdsOfPossibleToSentMessages(maxListSize: Int) =
        mailMessageRepository.findAllIdsByStateIn(possibleToSendMessageStates, maxListSize)

    suspend fun getMessageForSending(messageId: MailId): MailMessage {
        val messagesUpdated = mailMessageRepository.updateMessageStateAndSendingStartedTimeByIdAndStateIn(
            id = messageId,
            states = possibleToSendMessageStates,
            state = SENDING,
            sendingStartedAt = clock.instant(),
        )

        if (messagesUpdated == 0) {
            throw NotFoundException("MailMessage, id: $messageId for delivery is not found")
        }

        return mailMessageRepository.findOneWithTypeById(messageId)
            ?: throw NotFoundException("MailMessage, id: $messageId for delivery is not found")
    }

    suspend fun processSuccessfulDelivery(mailMessage: MailMessage) {
        mailMessage.apply {
            state = SENT
            sentAt = clock.instant()
        }

        mailMessageRepository.updateMessageStateAndSentTime(mailMessage.id, mailMessage.state, mailMessage.sentAt!!)
    }

    suspend fun processFailedDelivery(mailMessage: MailMessage) {
        mailMessage.apply {
            val maxRetries = type.maxRetriesCount ?: Int.MAX_VALUE

            if (failedCount >= maxRetries) {
                state = FAILED
                logger.error { "Max number of retries exceeded. Marking MailMessage: $id as failed" }
            } else {
                failedCount++
                state = RETRY
                logger.info { "Failed MailMessage: $id is scheduled for another delivery" }
            }

            sendingStartedAt = null
        }

        mailMessageRepository.updateMessageStateFailedCountAndSendingStartedTime(mailMessage.id, mailMessage.state, mailMessage.failedCount, null)
    }

    suspend fun processMessageTypeForceDeletion(mailMessage: MailMessage) {
        mailMessage.state = CANCELED

        mailMessageRepository.updateMessageState(mailMessage.id, mailMessage.state)
    }
}
