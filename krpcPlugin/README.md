# Binom RPC Tool Plugin
Kotlin Library for implementation RPC

## Using
### Gradle
You must add repository. See [README](../README.md)
```groovy
buildscript {
    dependencies {
        classpath "pw.binom.io:krpcPlugin:<version>"
    }
}

task generateDTO(type: pw.binom.krpc.KRpcTask) {
    outputDtoList="pw.binom.builder.remote.DTO_LIST" 
    destination = file("${buildDir}/gen")
    from(fileTree("src/commonMain/proto"))
    suspend = true
}
```
#### Plugin Settings:
|Name|Description|Example|
|---|----|----|
|outputDtoList|Package and variable name for dto generated dto list|`pw.binom.builder.remote.DTO_LIST`|
|destination|path for generated files|`file("${buildDir}/gen")`|
|from(...some file collection...)|path to source files|`from(fileTree("src/commonMain/proto"))`|

## Source files for generation
Using simple text files with special syntax.

### Types
You can define you Structs, also you can use brebuild types:

|Name|Description|Example|
|----|-----------|-------|
|int|Integer number|`int age`|
|long|Long number|`long time`|
|bool|Boolean|`bool sex`|
|float|Float number|`float height`|
|string|String line|`string name`|
|array&lt;another type&gt;|List of another elements|`array<string> names`|
|struct|Some another struct|`struct result`|

If you write `?` as suffix after type you set that this your field or argument
can contain `null` value. Аnd vice versa: if you not write `?` as suffix
then you field or argument won'y contain `null` value.<br>
Examples:
* `string name`
* `string? name`
* `array<string?> names`
* `array<string?>? names`
* `array<array<string>>? names`

### Package
Package for DTO and Interfaces<br>
Example:
```
package pw.binom.builder.remote
```

### Struct
You can define you struct using this:
```
struct <Name of struct> {
    <field type> <field name>
    <field type> <field name>
    ...
    <field type> <field name>
}
```

Example
```
package pw.binom.builder.remote

struct JobProcess {
    long buildNumber
    string path
}
```
As result of source above you got:
```kotlin
package pw.binom.builder.remote

import pw.binom.krpc.Struct
import pw.binom.krpc.StructFactory

class JobProcess(val buildNumber: Long, val path: String): Struct {
	companion object:StructFactory<JobProcess>{
		override val name
			get() = "pw.binom.builder.remote.JobProcess"
		override fun getField(dto: JobProcess, index: Int): Any? =
			when (index){
				0 -> dto.buildNumber
				1 -> dto.path
				else -> throw IllegalArgument("Can't find field with index $index")
			}
		override val uid
			get() = 120275895u
		override fun newInstance(fields: List<Any?>) =
			JobProcess(fields[0] as Long, fields[1] as String)
		override val fields =
			listOf<StructFactory.Field>(StructFactory.Field(0,"buildNumber",StructFactory.Class.Long(false)), StructFactory.Field(1,"path",StructFactory.Class.String(false)))
	}
	override val factory
		get() = JobProcess
}
```
### Interface
You can define you interfaces using this:
```
interface <Name of Interface> {
    <Result of function> <Method name>(<argument type> <argument name>, <argument type> <argument name>, ... , <argument type> <argument name>)
}
```

Example:
```
package pw.binom.builder.remote

interface NodesService {
    void pass(NodeDescription node, JobProcess? process)
}
```

As result of source above you got:
```kotlin
package pw.binom.builder.remote
object NodesService:RPCService<NodesServiceSync,NodesServiceAsync> {
	override val methods=
		listOf(
			RPCService.Method(0, "pass", listOf("node" to StructFactory.Class.Struct(NodeDescription ,false), "process" to StructFactory.Class.Struct(JobProcess ,true)), StructFactory.Class.Void))
	override fun call(service: NodesServiceSync, index: Int, args: List<Any?>): Any? =
		when (index){
			0 -> service.pass(args[0] as NodeDescription, args[1] as JobProcess?)
			else -> throw IllegalArgument("Can't find method with index $index")
	}
	override suspend fun callAsync(service: NodesServiceAsync, index: Int, args: List<Any?>): Any? =
		when (index){
			0 -> service.pass(args[0] as NodeDescription, args[1] as JobProcess?)
			else -> throw IllegalArgument("Can't find method with index $index")
	}
}

interface NodesServiceAsync {
	suspend fun pass(node: NodeDescription, process: JobProcess?):Unit
}

interface NodesServiceSync {
	fun pass(node: NodeDescription, process: JobProcess?):Unit
}
class NodesServiceRemoteSync(private val func:(RPCService.Method, List<Any?>)->Any?) {
	fun pass(node: NodeDescription, process: JobProcess?){
		func(NodesService.methods[0], listOf(node, process))
	}
}
class NodesServiceRemoteAsync(private val func: suspend (RPCService.Method, List<Any?>)->Any?) {
	suspend fun pass(node: NodeDescription, process: JobProcess?){
		func(NodesService.methods[0], listOf(node, process))
	}
}
```