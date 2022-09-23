package it.mail.persistence.common.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.io.ByteArrayOutputStream

fun KryoMailMessageDataSerializer(): KryoMailMessageDataSerializer {
    val kryo = Kryo().apply {

        register(Map::class.java)
        register(emptyMap<Nothing, Nothing>()::class.java)
        register(mapOf(null to null)::class.java)
        register(LinkedHashMap::class.java)

        register(List::class.java)
        register(listOf(null, null, null)::class.java)
    }

    return KryoMailMessageDataSerializer(kryo)
}

class KryoMailMessageDataSerializer(
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
