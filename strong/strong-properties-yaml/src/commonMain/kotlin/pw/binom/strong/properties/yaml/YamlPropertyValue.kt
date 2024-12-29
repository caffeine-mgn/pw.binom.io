package pw.binom.strong.properties.yaml

import com.charleskorn.kaml.YamlScalar
import pw.binom.properties.PropertyValue

internal class YamlPropertyValue(value: YamlScalar) : PropertyValue.Value {
  override val content: String = value.content
}
