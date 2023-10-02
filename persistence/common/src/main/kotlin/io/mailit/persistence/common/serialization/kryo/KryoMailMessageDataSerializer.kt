package io.mailit.persistence.common.serialization.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.DefaultSerializers
import com.esotericsoftware.kryo.util.Pool
import io.mailit.persistence.common.serialization.MailMessageDataSerializer

internal fun KryoMailMessageDataSerializer(): KryoMailMessageDataSerializer {
    val kryoPool = object : Pool<Kryo>(true, true) {
        override fun create() = createKryo()
    }

    return KryoMailMessageDataSerializer(kryoPool)
}

private fun createKryo() = Kryo().apply {
    // Registered type must be never removed and their ids must never change!
    register(String::class.java, DefaultSerializers.StringSerializer(), 1)
    register(Int::class.javaPrimitiveType, DefaultSerializers.IntSerializer(), 2)
    register(Long::class.javaPrimitiveType, DefaultSerializers.LongSerializer(), 3)
    register(Float::class.javaPrimitiveType, DefaultSerializers.FloatSerializer(), 4)
    register(Double::class.javaPrimitiveType, DefaultSerializers.DoubleSerializer(), 5)
    register(Boolean::class.javaPrimitiveType, DefaultSerializers.BooleanSerializer(), 6)

    register(Map::class.java, MailDataMapSerializer, 20)
    register(emptyMap<Nothing, Nothing>()::class.java, MailDataMapSerializer, 21)
    register(mapOf(1 to 1)::class.java, MailDataMapSerializer, 22)
    register(LinkedHashMap::class.java, MailDataMapSerializer, 23)
    register(HashMap::class.java, MailDataMapSerializer, 24)

    register(List::class.java, MailDataCollectionSerializer, 30)
    register(arrayOf<Unit>().asList()::class.java, MailDataCollectionSerializer, 31)
    register(emptyList<Nothing>()::class.java, MailDataCollectionSerializer, 32)
    register(listOf(1)::class.java, MailDataCollectionSerializer, 33)
    register(ArrayList::class.java, MailDataCollectionSerializer, 34)
}

internal class KryoMailMessageDataSerializer(
    private val kryoPool: Pool<Kryo>,
) : MailMessageDataSerializer {

    override fun write(data: Map<String, Any>?): ByteArray? {
        if (data == null) {
            return null
        }

        val output = Output(OUTPUT_BUFFER_SIZE, MAX_BUFFER_SIZE)

        kryoPool.withKryo { it.writeObject(output, data) }

        return output.toBytes()
    }

    override fun read(bytes: ByteArray?): Map<String, Any>? {
        if (bytes?.isEmpty() != false) {
            return null
        }

        val input = Input(bytes)

        return kryoPool.withKryo { it.readObject<Map<String, Any>>(input) }
    }

    companion object {
        private const val OUTPUT_BUFFER_SIZE = 1024
        private const val MAX_BUFFER_SIZE = -1 // Infinite.
    }
}

private inline fun <reified T> Kryo.readObject(input: Input): T = readObject(input, T::class.java)

private inline fun <T> Pool<Kryo>.withKryo(executable: (Kryo) -> T): T {
    val kryo = obtain()

    return try {
        executable(kryo)
    } finally {
        free(kryo)
    }
}
