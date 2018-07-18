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

package codegraft

import com.google.auto.service.AutoService
import codegraft.bootstrap.Package
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

@AutoService(Processor::class)
class AppProcessor : MainProcessor(true) {
    private var written = false

    override
    fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val returnVal = super.process(annotations, roundEnv)
        if (!roundEnv.processingOver() && bootstrap.isProcessed()) {
            if (!written) {
                written = true

                val applicationOutputs = bootstrap.currentRound.parentOutputs
                if (applicationOutputs.isNotEmpty()) {
                    write()
                } else {
                    bootstrap.env.log("no outputs")
                }
            }
        }
        return returnVal
    }

    private
    fun write() {
        val packageName = bootstrap.options.Package
        val file = bootstrap.processingEnv.filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            packageName,
            "Bootstrap_Gen.kt"
        )
        val src = src(packageName)
        file.openWriter().use {
            it.write(src)
            it.flush()
        }
    }
}

fun src(packageName: String): String = """/*
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

import codegraft.inject.android.AndroidApplication
import codegraft.inject.android.Bootstrap

typealias BootstrapInit = BootComponent.Builder.() -> AndroidApplication

fun bootstrap(
    init: BootstrapInit
): Bootstrap<AppComponent> = Bootstrap {
    DaggerBootComponent.builder()
            .build(init)
}

private
fun BootComponent.Builder.build(
    init: BootstrapInit
): AppComponent = application(init())
        .build()
        .appComponent

"""