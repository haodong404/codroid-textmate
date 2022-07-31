package org.codroid.textmate.theme

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer

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

    @Serializable(with = RawThemeSettingSerializer::class)
    var scopes: Array<ScopePattern>? = null,
    var scopesStr: String? = null,
    var settings: Setting? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawThemeSetting

        if (name != other.name) return false
        if (!scopesStr.contentEquals(other.scopesStr)) return false
        if (settings != other.settings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + scopesStr.hashCode()
        result = 31 * result + settings.hashCode()
        return result
    }
}

@OptIn(ExperimentalSerializationApi::class)
object RawThemeSettingSerializer :
    JsonTransformingSerializer<Array<RawThemeSetting>>(ArraySerializer(RawThemeSetting.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonArray) {
            return JsonArray(listOf(element))
        }
        return element
    }
}