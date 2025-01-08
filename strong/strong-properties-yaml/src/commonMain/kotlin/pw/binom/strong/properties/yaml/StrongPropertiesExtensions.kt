package pw.binom.strong.properties.yaml

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import pw.binom.properties.PropertyValue
import pw.binom.strong.properties.StrongProperties

fun StrongProperties.addYaml(
  text: String,
  yaml: Yaml = Yaml(configuration = YamlConfiguration(anchorsAndAliases = AnchorsAndAliases.Permitted())),
) {
  val obj = YamlConvertor.convert(yaml.parseToYamlNode(text))
  if (obj is PropertyValue.Object) {
    add(obj)
  }
}
