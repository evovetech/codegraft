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

package sourcerer

import com.squareup.javapoet.AnnotationSpec
import sourcerer.lib.LibComponentElement
import sourcerer.lib.LibComponentEnv
import sourcerer.lib.LibModuleEnv
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

/**
 * Created by layne on 3/8/18.
 */
class AppComponentWriter(
    private val pkg: Package,
    modules: LibModuleEnv,
    components: LibComponentEnv
) : SourceWriter {
    override val outKlass: Klass = pkg.AppComponent
    val rootComponent: LibComponentElement = components.run {
        readSourcererFiles()
        writeJavaFiles()
    }
    val env: BaseEnv = components
    //    val rootModule: LibModuleElement = modules.run {
//        readSourcererFiles()
//        writeJavaFiles()
//    }
    private val defaultModules: Array<Klass> = arrayOf(
        Dagger.Android.InjectionModule,
        pkg.AppModule,
        // TODO:
        Codegen.Inject.klass("InjectActivity_Collection"),
        Codegen.Inject.klass("LibModule_Collection")
    )

    private fun AnnotationSpec.Builder.addToModules() =
        addKlassTo("modules")

    override fun newBuilder() = outKlass.interfaceBuilder()

    override fun typeSpec() = typeSpec {
        addModifiers(PUBLIC)
        addAnnotation(JavaX.Inject.Singleton)
        addAnnotation(Dagger.Component) {
            defaultModules.forEach(addToModules())
//            addKlassTo("modules")(rootModule.outKlass)
        }
        addSuperinterface(Dagger.Android.Injector.parameterizedType(pkg.App.rawType))
        addSuperinterface(rootComponent.outKlass.rawType)
        addMethod("getApp", PUBLIC, ABSTRACT) {
            returns(pkg.App.rawType)
        }
        addType(Builder().typeSpec())
    }

    inner class Builder : SourceWriter {
        override val outKlass: Klass = "Builder".toKlass()

        override fun newBuilder() = outKlass.interfaceBuilder()

        override fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(Dagger.Component.nestedBuilder())
            addMethod("app", PUBLIC, ABSTRACT) {
                returns(outKlass.rawType)
                addAnnotation(Dagger.BindsInstance.rawType)
                addParameter(pkg.App.rawType, "app")
            }
            addMethod("build", PUBLIC, ABSTRACT) {
                returns(this@AppComponentWriter.outKlass.rawType)
            }
//            rootComponent.builders.forEach {
//                addSuperinterface(it)
//                env.elements()
//                        .getTypeElement(it.qualifiedName)
//                        .enclosedElements
//            }
        }
    }
}
