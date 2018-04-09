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

import com.squareup.javapoet.ClassName
import sourcerer.io.Writer
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

sealed
class Output

object NoOutput : Output()

class DeferredOutput(
    val element: Element
) : Output()

abstract
class BaseOutput : Output() {
    abstract
    fun writeTo(filer: Filer)
}

abstract
class JavaOutput(
    override val rawType: ClassName,
    override val outExt: String = ""
) : BaseOutput(),
    BaseElement {
    constructor(
        element: TypeElement,
        outExt: String
    ) : this(ClassName.get(element), outExt)
}

abstract
class SourcererOutput :
    BaseOutput() {
    abstract
    fun file(): MetaInf.File

    abstract
    fun write(writer: Writer)

    override
    fun writeTo(filer: Filer) = file()
            .newWriter(filer)
            .use(this::write)
}

/*

@LibComponent(modules=[SomeModule.class])
interface SomeComponent {
  SomeObject someObject();

  @LibComponent.Builder
  interface Builder {
    @BindsInstance Builder someObject(SomeObject object);
  }
}
  ->
     @Module(includes=[SomeModule.class])
     class SomeComponent_LibModule
-> LibModules.add(somecomponent_libmodule_javafile)

@LibModule(includes=[ClientModule.class])
class Network{}

  ->
     @Module(includes=[ClientModule.class])
     class Network_LibModule {}
-> LibModules.add(network_libmodule_javafile)



@Module(includes=[
    LibModules.all()
])
class LibModules {}

 */
