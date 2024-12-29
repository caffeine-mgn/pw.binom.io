package pw.binom.strong.properties.yaml

import com.charleskorn.kaml.YamlMap
import pw.binom.properties.PropertyValue

internal class YamlPropertyObject(map: YamlMap) : PropertyValue.Object {
  private val values = map.entries
    .asSequence()
    .map { (key, value) ->
      key.content to YamlConvertor.convert(value)
    }
    .toMap()
  override val names: Iterable<String> =
    values.keys


  override fun contains(key: String): Boolean =
    values.containsKey(key)

  override fun get(key: String): PropertyValue? =
    values[key]
}
