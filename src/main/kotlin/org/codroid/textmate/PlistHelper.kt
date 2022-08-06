package org.codroid.textmate

import com.dd.plist.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

inline fun <reified T : Any> decodeFromNSObject(dict: NSObject): T = decodeFromNSObject(dict, serializer())

fun <T> decodeFromNSObject(
    dict: NSObject, deserializationStrategy: DeserializationStrategy<T>
): T {
    val decoder = NSObjDecoder(dict, deserializationStrategy.descriptor)
    return decoder.decodeSerializableValue(deserializationStrategy)
}

/**
 * This decoder is used to convert NsObject to Kotlin entities.
 * It only supports [NSDictionary], [NSArray], [NSNumber], [NSString]
 */
@OptIn(ExperimentalSerializationApi::class)
internal open class NSObjDecoder(
    private val obj: NSObject?,
    private val descriptor: SerialDescriptor,
) : AbstractDecoder() {

    override val serializersModule: SerializersModule = EmptySerializersModule
    protected var elementIndex = 0
    private var elementCount = descriptor.elementsCount

    override fun decodeNotNullMark(): Boolean {
        return decodeValue() != "NULL"
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeValue(): Any {
        val descriptorNow = descriptor.getElementDescriptor(indexNow())
        val current = currentElement()
        return decodeValue(current, descriptorNow.kind)
    }

    @OptIn(ExperimentalSerializationApi::class)
    protected fun decodeValue(nsObject: NSObject?, kind: SerialKind?): Any {
        if (nsObject == null) return "NULL"
        return when (kind) {
            PrimitiveKind.STRING -> convString(nsObject)
            PrimitiveKind.INT -> convInt(nsObject)
            PrimitiveKind.LONG -> convLong(nsObject)
            PrimitiveKind.FLOAT -> convFloat(nsObject)
            PrimitiveKind.DOUBLE -> convDouble(nsObject)
            PrimitiveKind.BOOLEAN -> convBoolean(nsObject)

            StructureKind.CLASS,
            StructureKind.MAP,
            StructureKind.OBJECT,
            StructureKind.LIST -> nsObject

            else -> "NULL"
        }
    }

    protected fun convString(obj: NSObject): String = (obj as NSString).content

    protected fun convInt(obj: NSObject): Int = (obj as NSNumber).intValue()

    protected fun convLong(obj: NSObject): Long = (obj as NSNumber).longValue()

    protected fun convFloat(obj: NSObject): Float = (obj as NSNumber).floatValue()

    protected fun convDouble(obj: NSObject): Double = (obj as NSNumber).doubleValue()

    protected fun convBoolean(obj: NSObject): Boolean = (obj as NSNumber).boolValue()


    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementCount == elementIndex) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (elementIndex == 0) return this
        val currentInput = currentElement()
        if (descriptor.kind == StructureKind.LIST) {
            return ListDecoder(currentInput as NSArray?, descriptor)
        } else if (descriptor.kind == StructureKind.MAP) {
            return MapDecoder(currentInput as NSDictionary?, descriptor)
        }
        return NSObjDecoder(currentInput, descriptor)
    }

    private fun SerialDescriptor.getElementNameOrNull(index: Int): String? {
        return try {
            this.getElementName(index)
        } catch (_: Exception) {
            null
        }
    }

    fun currentElement(): NSObject? {
        if (obj !is NSDictionary) {
            return obj
        }
        return obj.hashMap[descriptor.getElementNameOrNull(indexNow())]
    }

    override fun decodeSequentially(): Boolean = false

    protected fun indexNow(): Int {
        return elementIndex - 1
    }

}

private class ListDecoder(
    private val obj: NSArray?, descriptor: SerialDescriptor
) : NSObjDecoder(obj, descriptor) {

    @OptIn(ExperimentalSerializationApi::class)
    private val elementKind = descriptor.elementDescriptors.firstOrNull()?.kind

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == obj?.count()) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeValue(): Any {
        obj?.array?.getOrNull(indexNow())?.let {
            return decodeValue(it, elementKind)
        }
        return "NULL"
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val currentInput = obj?.array?.getOrNull(indexNow())
        return when (descriptor.kind) {
            StructureKind.LIST -> ListDecoder(currentInput as NSArray?, descriptor)
            StructureKind.MAP -> MapDecoder(currentInput as NSDictionary?, descriptor)
            else -> NSObjDecoder(currentInput, descriptor)
        }
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return obj?.count() ?: 0
    }
}

private class MapDecoder(
    private val dict: NSDictionary?, descriptor: SerialDescriptor
) : NSObjDecoder(dict, descriptor) {

    private val dictIt = dict?.hashMap?.iterator()
    private var currentEntry: Map.Entry<String, NSObject>? = null

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == (dict?.count() ?: 0) * 2) return CompositeDecoder.DECODE_DONE
        if (elementIndex % 2 == 0 && dictIt?.hasNext() == true) {
            currentEntry = dictIt.next()
        }
        return elementIndex++
    }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> decodeValue(kind: PrimitiveKind): T {
        return if (elementIndex % 2 != 0) {
            currentEntry!!.key as T
        } else {
            decodeValue(currentEntry!!.value, kind) as T
        }
    }

    override fun decodeString(): String = decodeValue(PrimitiveKind.STRING)

    override fun decodeInt(): Int = decodeValue(PrimitiveKind.INT)

    override fun decodeLong(): Long = decodeValue(PrimitiveKind.LONG)

    override fun decodeFloat(): Float = decodeValue(PrimitiveKind.FLOAT)

    override fun decodeDouble(): Double = decodeValue(PrimitiveKind.DOUBLE)

    override fun decodeBoolean(): Boolean = decodeValue(PrimitiveKind.BOOLEAN)

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val currentInput = currentEntry!!.value
        return when (descriptor.kind) {
            StructureKind.LIST -> ListDecoder(currentInput as NSArray, descriptor)
            StructureKind.MAP -> MapDecoder(currentInput as NSDictionary, descriptor)
            else -> NSObjDecoder(currentInput, descriptor)
        }
    }

    override fun decodeSequentially(): Boolean = false

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return dict?.count() ?: 0
    }
}