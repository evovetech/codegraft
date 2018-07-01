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

package evovetech.sample

import sourcerer.inject.android.Bootstrap

//
// Generated
//

// package component
// application generated

fun bootstrap(boot: BootstrapInit) = Bootstrap {
    DaggerBootComponent.builder()
            .bootstrap(boot)
            .appComponent
}

private
typealias BootstrapInit = BootComponent.Builder.() -> App

private
fun BootComponent.Builder.bootstrap(init: BootstrapInit): BootComponent {
    val app = init()
    app(app)
    application(app)
    return build()
}
