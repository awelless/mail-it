package io.mailit.persistence.common.serialization.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

internal object MailDataMapSerializer : Serializer<Map<String, Any>>() {

    override fun write(kryo: Kryo, output: Output, map: Map<String, Any>) {
        val size = map.size
        output.writeVarInt(size, true)

        map.forEach { (key, value) ->
            output.writeString(key)
            kryo.writeClassAndObject(output, value)
        }
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Map<String, Any>>): Map<String, Any> {
        val size = input.readVarInt(true)

        val map = HashMap<String, Any>(size)

        for (i in 1..size) {
            val key = input.readString()
            val value = kryo.readClassAndObject(input)
            map[key] = value
        }

        return map
    }
}

internal object MailDataCollectionSerializer : Serializer<Collection<Any>>() {

    override fun write(kryo: Kryo, output: Output, collection: Collection<Any>) {
        val size = collection.size
        output.writeVarInt(size, true)

        collection.forEach { kryo.writeClassAndObject(output, it) }
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Collection<Any>>): Collection<Any> {
        val size = input.readVarInt(true)

        val collection = ArrayList<Any>(size)

        for (i in 1..size) {
            collection += kryo.readClassAndObject(input)
        }

        return collection
    }
}
