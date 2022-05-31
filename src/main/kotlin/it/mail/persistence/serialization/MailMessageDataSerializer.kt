package it.mail.persistence.serialization

internal interface MailMessageDataSerializer {

    fun write(data: Map<String, Any?>?): ByteArray

    fun read(bytes: ByteArray?): Map<String, Any?>?
}
