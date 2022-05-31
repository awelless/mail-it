package it.mail.persistence.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.io.ByteArrayOutputStream

internal fun KryoMailMessageDataSerializer(): KryoMailMessageDataSerializer {
    val kryo = Kryo()

    // this kryo instance is used only in data serializer
    kryo.isRegistrationRequired = false

    return KryoMailMessageDataSerializer(kryo)
}

internal class KryoMailMessageDataSerializer(
    private val kryo: Kryo,
) : MailMessageDataSerializer {

    override fun write(data: Map<String, Any?>?): ByteArray {
        if (data == null) {
            return ByteArray(0)
        }

        val outputStream = ByteArrayOutputStream()

        val output = Output(outputStream)
        kryo.writeClassAndObject(output, data)
        output.close()

        return outputStream.toByteArray()
    }

    override fun read(bytes: ByteArray?): Map<String, Any?>? {
        if (bytes?.isEmpty() != false) {
            return null
        }

        val input = Input(bytes)
        val value = kryo.readClassAndObjectUnsafe<Map<String, Any?>>(input)
        input.close()

        return value
    }
}

private inline fun <reified T> Kryo.readClassAndObjectUnsafe(input: Input): T {
    return readClassAndObject(input) as T
}
