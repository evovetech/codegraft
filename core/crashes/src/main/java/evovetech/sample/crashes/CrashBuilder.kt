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
import io.fabric.sdk.android.Fabric.Builder

interface CrashBuilder {
    fun Builder.init()
}


typealias CrashBuilderFunc = Fabric.Builder.() -> Unit

fun CrashBuilderFunc.toBuilder() = object : CrashBuilder {
    override
    fun Fabric.Builder.init() = this@toBuilder.invoke(this)
}

fun <B : CrashesComponent.Builder> B.crashes(
    init: CrashBuilderFunc
): B {
    crashes(init.toBuilder())
    return this
}
