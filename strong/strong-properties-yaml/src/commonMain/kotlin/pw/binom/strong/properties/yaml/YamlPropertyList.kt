package pw.binom.strong.properties.yaml

import com.charleskorn.kaml.YamlList
import pw.binom.properties.PropertyValue

internal class YamlPropertyList(value: YamlList) : PropertyValue.Enumerate {
  private val list = value.items.map {
    YamlConvertor.convert(it)
  }
  override val size: Int
    get() = list.size

  override fun get(index: Int): PropertyValue? =
    list[index]

}
