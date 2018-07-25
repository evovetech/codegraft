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

package codegraft.inject

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.reflect.KClass

@Target(ANNOTATION_CLASS)
@Retention(BINARY)
@MustBeDocumented
annotation
class GeneratePluginBindings(
    // TODO: maybe rename
    val pluginType: KClass<*>,

    // TODO: maybe rename
    val pluginTypeName: String = "",

    // TODO: maybe rename
    val pluginMapTypeName: String = "",

    val flattenComponent: Boolean = false
)