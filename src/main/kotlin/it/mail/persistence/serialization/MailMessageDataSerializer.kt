package it.mail.persistence.serialization

import javax.inject.Singleton

interface MailMessageDataSerializer {

    fun write(data: Map<String, Any?>?): ByteArray

    fun read(bytes: ByteArray?): Map<String, Any?>
}

// todo should be replaced with real serializer
@Singleton
class NoOpMailMessageDataSerializer : MailMessageDataSerializer {

    override fun write(data: Map<String, Any?>?): ByteArray = ByteArray(0)

    override fun read(bytes: ByteArray?): Map<String, Any?> = emptyMap()
}
