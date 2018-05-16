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

package sourcerer.inject.dev

import dagger.Module
import dagger.Subcomponent
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

open
class ModuleDescriptor(
    override val element: Element,
    val includes: Array<out TypeElement>,
    val subcomponents: Array<out TypeElement>
) : AnnotationDescriptor

annotation class Temp(
    val module: Module = Module(
        includes = [Module::class],
        subcomponents = [Subcomponent::class]
    )
)

@Temp(
    Module(
//        @get::AnnotatedWith()
        includes = [],
        subcomponents = []
    )
)
class NoOp

//val daggerModule = Module(
//    includes = [],
//    subcomponents = []
//)

/*

@Component(
    modules = [AppModule::class.java],
    dependencies = []
)
class AppComponent {
    // dependency methods
    val dep1: Dependency1
    val dep2: Dependency2

    interface Builder  {
        @BindsInstance fun dep1(dep1: Dependency1): Builder
        @BindsInstance fun dep2(dep2: Dependency2): Builder
        fun appModule(appModule: AppModule): Builder
        fun build(): AppComponent
    }
}

@Module(
    includes = [],
    subcomponents = []
)
class AppModule {

}
 */
