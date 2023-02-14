package io.mailit.persistence.common.serialization

interface MailMessageDataSerializer {

    fun write(data: Map<String, Any?>?): ByteArray

    fun read(bytes: ByteArray?): Map<String, Any?>?
}
