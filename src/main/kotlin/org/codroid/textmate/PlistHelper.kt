package org.codroid.textmate

import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.NSObject
import com.dd.plist.NSString
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import java.util.Dictionary

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
private open class NSObjDecoder(
    private val obj: NSObject,
    private val descriptor: SerialDescriptor,
) : AbstractDecoder() {

    override val serializersModule: SerializersModule = EmptySerializersModule
    private var elementIndex = -1
    private var elementCount = descriptor.elementsCount
    private var isCurrentNotNull = true

    override fun decodeNotNullMark(): Boolean {
        val result = decodeValue() != "NULL"
        if (result) {
            elementIndex--
        }
        return result
    }

    override fun decodeValue(): Any {
        elementIndex++
        isCurrentNotNull = true
        return when (val current = currentElement()) {
            is NSString -> {
                current.content
            }

            is NSNumber -> {
                if (current.isBoolean) {
                    return current.boolValue()
                } else if (current.isInteger) {
                    return current.intValue()
                }
                return current.stringValue()
            }

            else -> {
                isCurrentNotNull = false
                "NULL"
            }
        }
    }

    override fun decodeFloat(): Float {
        elementIndex++
        currentElement()?.let {
            if (it is NSNumber) {
                return it.floatValue()
            }
        }
        return 0.0F
    }


    override fun decodeDouble(): Double {
        elementIndex++
        currentElement()?.let {
            if (it is NSNumber) {
                return it.doubleValue()
            }
        }
        return 0.0
    }

    override fun decodeLong(): Long {
        elementIndex++
        currentElement()?.let {
            if (it is NSNumber) {
                return it.longValue()
            }
        }
        return 0L
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementCount == elementIndex) return CompositeDecoder.DECODE_DONE
        return elementIndex
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (elementIndex != -1) elementIndex++
        val currentInput = currentElement() ?: obj
        if (descriptor.kind == StructureKind.LIST) {
            return ListDecoder(currentInput as NSArray, descriptor)
        } else if (descriptor.kind == StructureKind.MAP) {
            return MapDecoder(currentInput as NSDictionary, descriptor)
        }
        return NSObjDecoder(currentInput, descriptor)
    }

    protected fun SerialDescriptor.getElementNameOrNull(index: Int): String? {
        return try {
            this.getElementName(index)
        } catch (_: Exception) {
            null
        }
    }

    private fun currentElement(): NSObject? {
        return (obj as NSDictionary).hashMap[descriptor.getElementNameOrNull(elementIndex)]
    }

    override fun decodeSequentially(): Boolean = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        if (obj is NSDictionary) {
            return obj.hashMap.size
        } else if (obj is NSArray) {
            return obj.array.size
        }
        return 0
    }
}

private class ListDecoder(
    private val obj: NSArray, private val descriptor: SerialDescriptor
) : NSObjDecoder(obj, descriptor) {

    var index = -1

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (index == descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
        return index
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        index++
        return NSObjDecoder(obj.array[index], descriptor)
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return obj.count()
    }
}

private class MapDecoder(
    private val dict: NSDictionary, private val descriptor: SerialDescriptor
) : NSObjDecoder(dict, descriptor) {

    private val dictIt = dict.hashMap.iterator()
    private var currentEntry: Map.Entry<String, NSObject>? = null

    override fun decodeString(): String {
        currentEntry = dictIt.next()
        return currentEntry!!.key
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return NSObjDecoder(currentEntry!!.value, descriptor)
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return dict.count()
    }
}
