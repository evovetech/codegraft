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

import dagger.MapKey
import dagger.Module
import dagger.multibindings.Multibinds
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

interface Plugin
typealias PluginClass = Class<out Plugin>
typealias PluginMap = Map<PluginClass, @JvmSuppressWildcards Plugin>
typealias PluginProviderMap = Map<PluginClass, @JvmSuppressWildcards Provider<Plugin>>

@Singleton
class Plugins
@Inject constructor(
    override val providers: PluginProviderMap
) : ClassKeyProviderMap<Plugin>

@MapKey
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation
class PluginKey(
    val value: KClass<out Plugin>
)

@LibComponent(modules = [PluginModule::class])
/* TODO: temp */
@LibModule(includes = [PluginModule::class])
interface PluginComponent {
    val plugins: Plugins
}

@Module
abstract class PluginModule {
    @Multibinds
    @Singleton
    abstract fun bindPlugins(): PluginMap
}
