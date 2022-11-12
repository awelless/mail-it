package it.mail.persistence.common.quarkus

import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers
import com.esotericsoftware.kryo.serializers.DefaultSerializers
import com.esotericsoftware.kryo.serializers.MapSerializer
import io.quarkus.runtime.annotations.RegisterForReflection
import it.mail.persistence.common.serialization.KryoMailMessageDataSerializer
import javax.inject.Singleton

@RegisterForReflection(
    targets = [
        DefaultArraySerializers.ByteArraySerializer::class,
        DefaultArraySerializers.CharArraySerializer::class,
        DefaultArraySerializers.ShortArraySerializer::class,
        DefaultArraySerializers.IntArraySerializer::class,
        DefaultArraySerializers.LongArraySerializer::class,
        DefaultArraySerializers.FloatArraySerializer::class,
        DefaultArraySerializers.DoubleArraySerializer::class,
        DefaultArraySerializers.BooleanArraySerializer::class,
        DefaultArraySerializers.StringArraySerializer::class,
        DefaultArraySerializers.ObjectArraySerializer::class,
        DefaultSerializers.BigIntegerSerializer::class,
        DefaultSerializers.BigDecimalSerializer::class,
        DefaultSerializers.ClassSerializer::class,
        DefaultSerializers.DateSerializer::class,
        DefaultSerializers.EnumSerializer::class,
        DefaultSerializers.EnumSetSerializer::class,
        DefaultSerializers.CurrencySerializer::class,
        DefaultSerializers.StringBufferSerializer::class,
        DefaultSerializers.StringBuilderSerializer::class,
        DefaultSerializers.CollectionsEmptyListSerializer::class,
        DefaultSerializers.CollectionsEmptyMapSerializer::class,
        DefaultSerializers.CollectionsEmptySetSerializer::class,
        DefaultSerializers.CollectionsSingletonListSerializer::class,
        DefaultSerializers.CollectionsSingletonMapSerializer::class,
        DefaultSerializers.CollectionsSingletonSetSerializer::class,
        DefaultSerializers.TreeSetSerializer::class,
        CollectionSerializer::class,
        DefaultSerializers.ConcurrentSkipListMapSerializer::class,
        DefaultSerializers.TreeMapSerializer::class,
        MapSerializer::class,
        DefaultSerializers.TimeZoneSerializer::class,
        DefaultSerializers.CalendarSerializer::class,
        DefaultSerializers.LocaleSerializer::class,
        DefaultSerializers.CharsetSerializer::class,
        DefaultSerializers.URLSerializer::class,
        DefaultSerializers.ArraysAsListSerializer::class,
        DefaultSerializers.VoidSerializer::class,
        DefaultSerializers.PriorityQueueSerializer::class,
        DefaultSerializers.BitSetSerializer::class,
        DefaultSerializers.KryoSerializableSerializer::class,
    ]
)
class SerializationConfiguration {

    @Singleton
    fun kryoMailMessageDataSerializer() = KryoMailMessageDataSerializer()
}
