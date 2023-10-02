package io.mailit.persistence.postgresql

internal object Tables {
    const val API_KEY = "api_key"
    const val INSTANCE_ID_LOCKS = "instance_id_locks"
    const val MAIL_MESSAGE = "mail_message"
    const val MAIL_MESSAGE_TEMPLATE = "mail_message_template"
    const val MAIL_MESSAGE_TYPE = "mail_message_type"
}

internal object Columns {

    object ApiKey {
        const val ID = "api_api_key_id"
        const val NAME = "api_name"
        const val SECRET = "api_secret"
        const val CREATED_AT = "api_created_at"
        const val EXPIRES_AT = "api_expires_at"
    }

    object MailMessage {
        const val ID = "m_mail_message_id"
        const val TEXT = "m_text"
        const val DATA = "m_data"
        const val SUBJECT = "m_subject"
        const val EMAIL_FROM = "m_email_from"
        const val EMAIL_TO = "m_email_to"
        const val CREATED_AT = "m_created_at"
        const val SENDING_STARTED_AT = "m_sending_started_at"
        const val SENT_AT = "m_sent_at"
        const val STATUS = "m_status"
        const val FAILED_COUNT = "m_failed_count"
        const val DEDUPLICATION_ID = "m_deduplication_id"
    }

    object MailMessageType {
        const val ID = "mt_mail_message_type_id"
        const val NAME = "mt_name"
        const val DESCRIPTION = "mt_description"
        const val MAX_RETRIES_COUNT = "mt_max_retries_count"
        const val STATE = "mt_state"
        const val CREATED_AT = "mt_created_at"
        const val UPDATED_AT = "mt_updated_at"
        const val CONTENT_TYPE = "mt_content_type"
        const val TEMPLATE_ENGINE = "mt_template_engine"
        const val TEMPLATE = "mt_template"
    }
}
