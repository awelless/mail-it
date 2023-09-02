package io.mailit.core.model

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.text.Charsets.UTF_8

@JvmInline
value class MailMessageTemplate(val value: String) {

    val compressedValue: ByteArray
        get() = ByteArrayOutputStream().use {
            GZIPOutputStream(it).apply {
                write(value.toByteArray(UTF_8))
                close()
            }
            it.toByteArray()
        }

    companion object {
        fun fromCompressedValue(compressedValue: ByteArray) = MailMessageTemplate(
            GZIPInputStream(ByteArrayInputStream(compressedValue), compressedValue.size).readAllBytes().toString(UTF_8),
        )
    }
}
