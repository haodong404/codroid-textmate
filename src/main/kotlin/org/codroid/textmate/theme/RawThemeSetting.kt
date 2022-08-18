package org.codroid.textmate.theme

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import org.codroid.textmate.IntBooleanSerializer
import org.codroid.textmate.ListDecoder
import org.codroid.textmate.NSObjDecoder
import org.codroid.textmate.json

@Serializable
data class Setting(
    var fontStyle: String? = null, var foreground: String? = null, var background: String? = null
) {
    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other !is Setting) {
                return false
            }
            return fontStyle == other.fontStyle && foreground == other.foreground && background == other.background
        }
        return false
    }

    override fun hashCode(): Int {
        var result = fontStyle?.hashCode() ?: 0
        result = 31 * result + (foreground?.hashCode() ?: 0)
        result = 31 * result + (background?.hashCode() ?: 0)
        return result
    }
}

@Serializable
data class RawThemeSetting(
    var name: String? = null,

    @Serializable(with = RawThemeSettingScopeSerializer::class)
    var scope: Array<ScopePattern>? = null,
    var settings: Setting? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawThemeSetting

        if (name != other.name) return false
        if (!scope.contentEquals(other.scope)) return false
        if (settings != other.settings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + scope.hashCode()
        result = 31 * result + settings.hashCode()
        return result
    }
}

@OptIn(ExperimentalSerializationApi::class)
object RawThemeSettingScopeSerializer :
    KSerializer<Array<String>> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RawThemeSettingScope", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Array<String> {
        if (decoder is NSObjDecoder) {
            return deserializeWithNSObjectDecoder(decoder)
        }
        val jsonElement = (decoder as JsonDecoder).decodeJsonElement()
        if (jsonElement !is JsonArray) {
            jsonElement.jsonPrimitive.content.let {
                return scopesConstruct(it)
            }
        }
        return jsonElement.jsonArray.map {
            it.jsonPrimitive.content
        }.toTypedArray()
    }

    private fun deserializeWithNSObjectDecoder(decoder: NSObjDecoder): Array<String> {
        val result = decoder.decodeValue()
        if (result is String) {
            return scopesConstruct(result)
        }
        return result as Array<String>
    }

    private fun scopesConstruct(scopes: String): Array<String> {
        val scope = StringBuilder(scopes.trim())
        if (scope.startsWith(',')) {   // remove leading commas
            scope.deleteAt(0)
        }
        if (scope.endsWith(',')) {  // remove trailing commas
            scope.deleteAt(scope.length - 1)
        }
        return scope.split(',').toTypedArray()
    }

    override fun serialize(encoder: Encoder, value: Array<String>) {
        TODO("Not yet implemented")
    }
}