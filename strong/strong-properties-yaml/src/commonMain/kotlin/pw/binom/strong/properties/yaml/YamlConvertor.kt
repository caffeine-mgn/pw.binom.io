package pw.binom.strong.properties.yaml

import com.charleskorn.kaml.*
import pw.binom.properties.PropertyValue

internal object YamlConvertor {
    fun convert(node: YamlNode): PropertyValue? =
        when (node) {
            is YamlList -> YamlPropertyList(node)
            is YamlMap -> YamlPropertyObject(node)
            is YamlNull -> YamlPropertyNull
            is YamlScalar -> YamlPropertyValue(node)
            is YamlTaggedNode -> convert(node.innerNode)
        }
}
