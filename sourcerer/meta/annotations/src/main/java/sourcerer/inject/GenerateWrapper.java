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

package sourcerer.inject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Documented
@Repeatable(GenerateWrappers.class)
//@Target(ANNOTATION_TYPE)
@Retention(SOURCE)
public
@interface GenerateWrapper {
    Class<? extends Annotation> value();

    String name() default "";
}

/*

@MustBeDocumented
@Retention(BINARY)
@Repeatable
annotation
class GenerateWrapper(
    val value: KClass<out Annotation>,
    val name: String = ""
)

 */
