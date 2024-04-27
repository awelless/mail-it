package io.mailit.worker.core

import io.mailit.value.MailTypeId
import io.mailit.value.MailTypeState
import io.mailit.value.TemplateEngine

internal sealed class MailType(
    val id: MailTypeId,
    val state: MailTypeState,
    maxRetriesCount: Int?,
) {
    val maxRetriesCount = maxRetriesCount ?: Int.MAX_VALUE

    fun shouldCancelMessageSending() = state == MailTypeState.FORCE_DELETED
}

internal class PlainMailType(
    id: MailTypeId,
    state: MailTypeState,
    maxRetriesCount: Int?,
) : MailType(
    id = id,
    state = state,
    maxRetriesCount = maxRetriesCount,
)

internal class HtmlMailType(
    id: MailTypeId,
    state: MailTypeState,
    maxRetriesCount: Int?,

    val templateEngine: TemplateEngine,
) : MailType(
    id = id,
    state = state,
    maxRetriesCount = maxRetriesCount,
)
