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

package sourcerer.inject

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FILE
import kotlin.reflect.KClass

/**
 * Created by layne on 2/26/18.
 */

@IntoCollection(InjectActivity::class)
@MustBeDocumented
@Target(
    CLASS,
    FILE
)
@Retention(
    BINARY
)
annotation
class InjectActivity(
    /**
     * The generated `@Module` class name.
     */
    val name: String = "",
    /**
     * Additional `@Module`-annotated classes from which this activity is
     * composed.
     */
    val includes: Array<KClass<*>> = []
)
