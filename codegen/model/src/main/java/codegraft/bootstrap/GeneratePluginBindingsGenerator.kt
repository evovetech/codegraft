/*
 * Copyright 2018 evove.tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package codegraft.bootstrap

import com.google.auto.common.MoreElements
import com.squareup.javapoet.ClassName
import dagger.internal.codegen.uniqueScope
import dagger.model.Scope
import sourcerer.KotlinOutput
import sourcerer.Outputs
import sourcerer.name
import java.io.Writer
import javax.inject.Inject

/*
typealias PluginMap = ClassMap<Plugin>
typealias PluginProviderMap = ClassProviderMap<Plugin>

@javax.inject.Singleton
class Plugins
@Inject constructor(
    override val providers: PluginProviderMap
) : ClassKeyProviderMap<Plugin>

@MapKey
@MustBeDocumented
@Retention(RUNTIME)
annotation
class PluginKey(
    val value: KClass<out Plugin>
)

@Module
interface PluginModule {
    @Multibinds
    @javax.inject.Singleton
    fun bindPlugins(): ClassMap<Plugin>
}

@BootstrapComponent(

)
interface PluginComponent {
    val plugins: Plugins
}
*/


class GeneratePluginBindingsGenerator(
    private val descriptor: GeneratePluginBindingsDescriptor,
    private val elementType: ClassName = ClassName.get(descriptor.element)
) {
    private
    val packageName: String = elementType.packageName()

    fun process(): Outputs {
        return listOf(
            TypeAliasGenerator()
        )
    }

    inner
    class TypeAliasGenerator() : KotlinOutput(
        packageName = packageName,
        fileName = "${elementType.name}_Gen"
    ) {
        override
        fun writeTo(writer: Writer) {
            val pluginType = descriptor.pluginType
            val scope = descriptor.element.uniqueScope
            val src = codegraft.bootstrap.pluginSrc(
                packageName = packageName,
                Type = pluginType.simpleName.toString(),
                Types = descriptor.pluralName,
                Scope = scope.annotationName
            )
            writer.write(src)
        }
    }

    class Factory
    @Inject constructor(

    ) {
        fun create(
            descriptor: GeneratePluginBindingsDescriptor
        ): GeneratePluginBindingsGenerator {
            return GeneratePluginBindingsGenerator(descriptor)
        }
    }
}

val Scope?.annotationName: String
    get() {
        return if (this == null) {
            ""
        } else {
            val annotationType = scopeAnnotation().annotationType
            val annotationElement = MoreElements.asType(annotationType.asElement())
            "@${annotationElement.qualifiedName}"
        }
    }

fun pluginSrc(
    packageName: String,
    Type: String,
    Types: String = Type + "s",
    Scope: String = "@Singleton"
): String = """
/*
 * Copyright 2018 evove.tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// Generated
//
package $packageName

import codegraft.inject.ClassKeyProviderMap
import codegraft.inject.ClassMap
import codegraft.inject.ClassProviderMap
import dagger.MapKey
import dagger.Module
import dagger.multibindings.Multibinds
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

typealias ${Type}Map = ClassMap<$Type>
typealias ${Type}ProviderMap = ClassProviderMap<$Type>

$Scope
class $Types
@Inject constructor(
    override val providers: ${Type}ProviderMap
) : ClassKeyProviderMap<$Type>()

@MapKey
@MustBeDocumented
@Retention(RUNTIME)
annotation
class ${Type}Key(
    val value: KClass<out $Type>
)

@Module
interface ${Type}Module {
    @Multibinds
    $Scope
    fun bind$Types(): ${Type}Map
}

""".trimIndent()
