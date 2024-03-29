package io.mailit.persistence.common.serialization.kryo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

open class KryoMailMessageDataSerializerTest {

    private val serializer = KryoMailMessageDataSerializer()

    @ParameterizedTest
    @MethodSource("dataForSerialization")
    fun `serialize and back`(data: Map<String, Any>) {
        val bytes = serializer.write(data)
        val actual = serializer.read(bytes)

        assertEquals(data, actual)
    }

    companion object {
        @JvmStatic
        fun dataForSerialization(): List<Map<String, Any>> = listOf(
            emptyMap(),
            mapOf("int" to 123),
            mapOf("long" to 123L),
            mapOf("float" to 12.3f),
            mapOf("double" to 12.3),
            mapOf("boolean" to true),
            mapOf("string" to "123"),
            mapOf("intList" to listOf(1, 2, 3, 4)),
            mapOf("doubleList" to listOf(1.2, 2.3, 3.4, 4.5)),
            mapOf("emptyList" to emptyList<Unit>()),
            mapOf("listWithDifferentTypes" to listOf(1, "2", mapOf("3" to 4.5))),
            mapOf("intMap" to mapOf("1" to 2, "3" to 4)),
            mapOf("doubleMap" to mapOf("1.2" to 2.3, "3.4" to 4.5)),
            mapOf("emptyMap" to emptyMap<Unit, Unit>()),
            mapOf("linkedHashMap" to LinkedHashMap<String, Any>().apply { put("1", 2) }),
            mapOf(
                "name" to "John",
                "age" to 20,
                "phones" to listOf("12345", "24657"),
                "purchases" to mapOf(
                    "oranges" to 2,
                    "apples" to 4,
                ),
            ),
        )
    }
}
