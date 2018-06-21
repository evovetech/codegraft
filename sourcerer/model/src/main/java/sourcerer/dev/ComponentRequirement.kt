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

package sourcerer.dev

import com.google.auto.common.MoreElements.getLocalAndInheritedMethods
import com.google.auto.common.MoreTypes
import com.google.common.base.Equivalence
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Provides
import dagger.internal.codegen.SourcererElements.Companion.isAnyAnnotationPresent
import dagger.internal.codegen.componentCanMakeNewInstances
import dagger.internal.codegen.simpleVariableName
import dagger.model.Key
import dagger.multibindings.Multibinds
import dagger.producers.Produces
import sourcerer.dev.ComponentRequirement.Kind.BOUND_INSTANCE
import sourcerer.dev.ComponentRequirement.Kind.DEPENDENCY
import sourcerer.dev.ComponentRequirement.Kind.MODULE
import sourcerer.dev.ComponentRequirement.NullPolicy.ALLOW
import java.util.Optional
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/** A type that a component needs an instance of.  */
internal data
class ComponentRequirement(
    /** The kind of requirement.  */
    val kind: Kind,

    /**
     * The type of the instance the component must have, wrapped so that requirements can be used as
     * value types.
     */
    val wrappedType: Equivalence.Wrapper<TypeMirror>,

    /**
     * An override for the requirement's null policy. If set, this is used as the null policy instead
     * of the default behavior in [.nullPolicy].
     *
     *
     * Some implementations' null policy can be determined upon construction (e.g., for binding
     * instances), but others' require Elements and Types, which must wait until [.nullPolicy]
     * is called.
     */
    val overrideNullPolicy: Optional<NullPolicy>,


    /** The key for this requirement, if one is available.  */
    val key: Optional<Key>,

    /** Returns the name for this requirement that could be used as a variable.  */
    val variableName: String
) {
    enum
    class Kind {
        /** A type listed in the component's `dependencies` attribute.  */
        DEPENDENCY,

        /** A type listed in the component or subcomponent's `modules` attribute.  */
        MODULE,

        /**
         * An object that is passed to a builder's [@BindsInstance][dagger.BindsInstance] method.
         */
        BOUND_INSTANCE
    }

    /** The type of the instance the component must have.  */
    val type: TypeMirror
        get() = wrappedType.get()!!

    /** The element associated with the type of this requirement.  */
    val typeElement: TypeElement
        get() = MoreTypes.asTypeElement(type)

    /** The action a component builder should take if it `null` is passed.  */
    enum
    class NullPolicy {
        /** Make a new instance.  */
        NEW,
        /** Throw an exception.  */
        THROW,
        /** Allow use of null values.  */
        ALLOW
    }

    /** The requirement's null policy.  */
    fun nullPolicy(elements: Elements, types: Types): NullPolicy {
        if (overrideNullPolicy.isPresent) {
            return overrideNullPolicy.get()
        }
        return when (kind) {
            ComponentRequirement.Kind.MODULE -> when {
                componentCanMakeNewInstances(typeElement) -> NullPolicy.NEW
                requiresAPassedInstance(elements, types) -> NullPolicy.THROW
                else -> NullPolicy.ALLOW
            }
            ComponentRequirement.Kind.DEPENDENCY, ComponentRequirement.Kind.BOUND_INSTANCE -> NullPolicy.THROW
        }
    }

    /**
     * Returns true if the passed [ComponentRequirement] requires a passed instance in order
     * to be used within a component.
     */
    fun requiresAPassedInstance(elements: Elements, types: Types): Boolean {
        if (kind == Kind.BOUND_INSTANCE) {
            // A user has explicitly defined in their component builder they will provide an instance.
            return true
        }

        val methods = getLocalAndInheritedMethods(typeElement, types, elements)
        var foundInstanceMethod = false
        for (method in methods) {
            if (method.getModifiers().contains(ABSTRACT) && !isAnyAnnotationPresent(
                    method, Binds::class.java, Multibinds::class.java, BindsOptionalOf::class.java
                )) {
                // TODO(ronshapiro): it would be cool to have internal meta-annotations that could describe
                // these, like @AbstractBindingMethod
                /* We found an abstract method that isn't a binding method. That automatically means that
         * a user will have to provide an instance because we don't know which subclass to use. */
                return true
            } else if (!method.modifiers.contains(STATIC) && isAnyAnnotationPresent(
                    method, Provides::class.java,
                    Produces::class.java
                )) {
                foundInstanceMethod = true
            }
        }

        return if (foundInstanceMethod) {
            !componentCanMakeNewInstances(typeElement)
        } else false

    }

    companion object {
        fun forDependency(type: TypeMirror): ComponentRequirement {
            return ComponentRequirement(
                DEPENDENCY,
                MoreTypes.equivalence().wrap(checkNotNull(type)),
                Optional.empty(),
                Optional.empty(),
                simpleVariableName(MoreTypes.asTypeElement(type))
            )
        }

        fun forModule(type: TypeMirror): ComponentRequirement {
            return ComponentRequirement(
                MODULE,
                MoreTypes.equivalence().wrap(checkNotNull(type)),
                Optional.empty(),
                Optional.empty(),
                simpleVariableName(MoreTypes.asTypeElement(type))
            )
        }

        fun forBoundInstance(key: Key, nullable: Boolean, variableName: String): ComponentRequirement {
            return ComponentRequirement(
                BOUND_INSTANCE,
                MoreTypes.equivalence().wrap(key.type()),
                if (nullable) Optional.of(ALLOW) else Optional.empty(),
                Optional.of(key),
                variableName
            )
        }

//        fun forBoundInstance(binding: ContributionBinding): ComponentRequirement {
//            checkArgument(binding.kind() == BindingKind.BOUND_INSTANCE)
//            return forBoundInstance(
//                binding.key(),
//                binding.nullableType().isPresent,
//                binding.bindingElement().get().simpleName.toString()
//            )
//        }
    }
}
