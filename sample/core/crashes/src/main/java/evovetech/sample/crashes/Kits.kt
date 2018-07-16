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

package evovetech.sample.crashes

import io.fabric.sdk.android.Fabric
import io.fabric.sdk.android.Kit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class Kits
@Inject constructor(
    fabric: Fabric
) {
    private val map = fabric.kits
            .groupBy { it::class }
            .mapValues { it.value.first()!! }

    operator
    fun <T : Kit<*>> get(clazz: KClass<T>): T {
        val kit = map[clazz]
        return clazz.java.cast(kit)
    }

    inline
    fun <reified T : Kit<*>> get(): T {
        return get(T::class)
    }
}
