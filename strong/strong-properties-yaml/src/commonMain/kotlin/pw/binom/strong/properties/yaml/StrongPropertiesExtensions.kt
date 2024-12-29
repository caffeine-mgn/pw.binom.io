package pw.binom.strong.properties.yaml

import com.charleskorn.kaml.Yaml
import pw.binom.properties.PropertyValue
import pw.binom.strong.properties.StrongProperties

fun StrongProperties.addYaml(yaml: String) {
  val obj = YamlConvertor.convert(Yaml.default.parseToYamlNode(yaml))
  if (obj is PropertyValue.Object) {
    add(obj)
  }
}
