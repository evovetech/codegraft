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

package sourcerer.activity

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import dagger.Module
import sourcerer.AnnotationStep
import sourcerer.Codegen
import sourcerer.CollectionStep
import sourcerer.Dagger
import sourcerer.Env
import sourcerer.Input
import sourcerer.Intermediate
import sourcerer.IntoCollectionStep
import sourcerer.JavaOutput
import sourcerer.Klass
import sourcerer.NoOutput
import sourcerer.Output
import sourcerer.StoredFile
import sourcerer.addAnnotation
import sourcerer.addMethod
import sourcerer.addTo
import sourcerer.inject.InjectActivity
import sourcerer.inject.IntoCollection
import sourcerer.intoCollection
import sourcerer.io.Writer
import sourcerer.javaClassOutput
import sourcerer.name
import javax.lang.model.element.Modifier

object ActivityAnnotationStep : AnnotationStep<InjectActivity> {
    override
    fun invoke(
        env: Env,
        input: Input<InjectActivity>
    ) = env.javaClassOutput(input, "Module_2", "Injected Activity Module") {
        addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        addAnnotation(Dagger.Module)
        intoCollection<InjectActivity>()
        addMethod("contribute${it.rawType.name}") {
            addAnnotation(Codegen.Inject.ActivityScope.rawType)
            addAnnotation(Dagger.Android.ContributesInjector.rawType)
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            returns(it.rawType)
        }
    }
}

class ActivityCollectionStep : IntoCollectionStep(InjectActivity::class) {
    override
    fun process(env: Env, inputs: List<Input<IntoCollection>>): Output {
        inputs.forEach {
            env.log(it.element, "IntoCollection<InjectActivity>")
        }
        return NoOutput
    }
}

class ActivityStep(
    isModule: Boolean
) : CollectionStep<InjectActivity>(
    isModule = isModule,
    inputType = InjectActivity::class,
    collector = ActivityAnnotationStep
) {
    override
    fun Writer.writeAll(
        processed: Collection<Intermediate<InjectActivity>>
    ): Unit = processed.classNames()
            .let { writeTypeNames(it) }

    override
    fun TypeSpec.Builder.writeAll(
        env: Env,
        processed: Collection<Intermediate<InjectActivity>>,
        stored: Collection<StoredFile>
    ) {
        val classNames = processed.classNames() + stored.readAll()
        addModifiers(Modifier.PUBLIC)
        addAnnotation(Dagger.Module) {
            val addToIncludes = addTo("includes")
            classNames.forEach(addToIncludes)
        }
        intoCollection<Module>()
    }

    private
    fun StoredFile.readAll(): List<ClassName> = this
            .read { reader -> reader.readTypeNames() }
            .mapNotNull { it as? ClassName }

    private
    fun Collection<StoredFile>.readAll(): List<ClassName> = this
            .flatMap { it.readAll() }

    private
    fun Collection<Intermediate<InjectActivity>>.klasses(): List<Klass> = this
            .mapNotNull { it.output as? JavaOutput }
            .map(JavaOutput::outKlass)

    private
    fun Collection<Intermediate<InjectActivity>>.classNames(): List<ClassName> = this
            .klasses()
            .map(Klass::rawType)
}

/*
class JoinActivitiesStep {

    dependson InjectActivity::class

    val out = ArrayList()

    fun process(annotations, outputs<InjectActivity>) {
        if (!outputs.isEmpty() {
            out.addAll(outputs)
            return DeferredOutput
        }
        out.addAll(loadJars())
        return write(out)
    }
}
 */
