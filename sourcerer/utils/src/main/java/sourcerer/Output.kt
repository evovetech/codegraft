/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package sourcerer

import com.squareup.javapoet.ClassName
import sourcerer.MetaInf.File
import sourcerer.io.Writer
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

sealed
class Output

object NoOutput : Output()

class DeferredOutput(
    val element: Element
) : Output()

abstract
class BaseOutput : Output(),
    Includable {
    override
    val include: Boolean
        get() = true

    abstract
    fun writeTo(filer: Filer)
}

abstract
class KotlinOutput(
    val packageName: String,
    val fileName: String
) : BaseOutput() {
    constructor(
        className: ClassName
    ) : this(className.packageName(), className.name)

    abstract
    fun writeTo(writer: java.io.Writer)

    final override
    fun writeTo(filer: Filer) {
        val file = filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            packageName,
            "$fileName.kt"
        )

        file.openWriter().use { writer ->
            writeTo(writer)
            writer.flush()
        }
    }
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

    abstract
    class Builder : SourceWriter {
        override
        val outKlass: Klass = "Builder".toKlass()

        override
        fun newBuilder() = outKlass.interfaceBuilder()
    }
}

abstract
class SourcererOutput :
    BaseOutput() {
    abstract
    fun file(): File

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
