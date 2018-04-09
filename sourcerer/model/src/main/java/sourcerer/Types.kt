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

object Codegen : DefaultPackage("sourcerer") {
    val LibComponents = klass("LibComponents")
    val LibModules = klass("LibModules")

    object Inject : Package.SubPackage(this, "inject") {
        val Generated = klass("Generated")
        val ActivityScope = klass("ActivityScope")
        val IntoCollection = klass("IntoCollection")
    }
}

object Dagger : DefaultPackage("dagger") {
    val Component = klass("Component")
    val Module = klass("Module")
    val MapKey = klass("MapKey")
    val BindsInstance = klass("BindsInstance")

    object Android : Package.SubPackage(this, "android") {
        val InjectionModule = klass("AndroidInjectionModule")
        val Injector = klass("AndroidInjector")
        val ContributesInjector = klass("ContributesAndroidInjector")
    }
}

object JavaX : DefaultPackage("javax") {
    object Inject : Package.SubPackage(this, "inject") {
        val Singleton = klass("Singleton")
    }
}
