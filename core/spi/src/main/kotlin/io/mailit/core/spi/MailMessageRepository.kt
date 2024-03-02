package io.mailit.core.spi

import io.mailit.core.model.MailMessage
import io.mailit.core.model.Slice
import io.mailit.value.MailId
import io.mailit.value.MailState
import java.time.Instant

interface MailMessageRepository {

    suspend fun findOneWithTypeById(id: MailId): MailMessage?

    suspend fun findAllWithTypeByStatesAndSendingStartedBefore(
        states: Collection<MailState>,
        sendingStartedBefore: Instant,
        maxListSize: Int,
    ): List<MailMessage>

    suspend fun findAllIdsByStateIn(states: Collection<MailState>, maxListSize: Int): List<MailId>

    /**
     * Returns a [Slice] of specified zero indexed [page] and [size]
     */
    suspend fun findAllSlicedDescendingIdSorted(page: Int, size: Int): Slice<MailMessage>

    suspend fun updateMessageState(id: MailId, state: MailState): Int

    suspend fun updateMessageStateAndSendingStartedTimeByIdAndStateIn(
        id: MailId,
        states: Collection<MailState>,
        state: MailState,
        sendingStartedAt: Instant,
    ): Int

    suspend fun updateMessageStateAndSentTime(id: MailId, state: MailState, sentAt: Instant): Int

    suspend fun updateMessageStateFailedCountAndSendingStartedTime(id: MailId, state: MailState, failedCount: Int, sendingStartedAt: Instant?): Int
}
