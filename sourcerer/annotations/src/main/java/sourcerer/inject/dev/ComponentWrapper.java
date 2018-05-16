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

package sourcerer.inject.dev;

import dagger.Component;
import dagger.Module;
import sourcerer.inject.Wraps;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Wraps(Component.class)
public
@interface ComponentWrapper {
    /**
     * A list of classes annotated with {@link Module} whose bindings are used to generate the
     * component implementation. Note that through the use of {@link Module#includes} the full set of
     * modules used to implement the component may include more modules that just those listed here.
     */
    Class<?>[] modules() default {};

    /**
     * A list of types that are to be used as <a href="#component-dependencies">component
     * dependencies</a>.
     */
    Class<?>[] dependencies() default {};

    /**
     * A builder for a component. Components may have a single nested static abstract class or
     * interface annotated with {@code @Component.Builder}.  If they do, then the component's
     * generated builder will match the API in the type.  Builders must follow some rules:
     * <ul>
     * <li> A single abstract method with no arguments must exist, and must return the component.
     * (This is typically the {@code build()} method.)
     * <li> All other abstract methods must take a single argument and must return void,
     * the Builder type, or a supertype of the builder.
     * <li> Each component dependency <b>must</b> have an abstract setter method.
     * <li> Each module dependency that Dagger can't instantiate itself (e.g, the module
     * doesn't have a visible no-args constructor) <b>must</b> have an abstract setter method.
     * Other module dependencies (ones that Dagger can instantiate) are allowed, but not required.
     * <li> Non-abstract methods are allowed, but ignored as far as validation and builder generation
     * are concerned.
     * </ul>
     * <p>
     * For example, this could be a valid Component with a Builder: <pre><code>
     * {@literal @}Component(modules = {BackendModule.class, FrontendModule.class})
     * interface MyComponent {
     *   MyWidget myWidget();
     *
     *   {@literal @}Component.Builder
     *   interface Builder {
     *     MyComponent build();
     *     Builder backendModule(BackendModule bm);
     *     Builder frontendModule(FrontendModule fm);
     *   }
     * }</code></pre>
     */
    @Target(TYPE)
    @Documented
    @Wraps(Component.Builder.class)
    @interface Builder {}
}
