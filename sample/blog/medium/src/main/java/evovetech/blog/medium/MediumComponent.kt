/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package evovetech.blog.medium

import codegraft.inject.BootstrapComponent
import codegraft.inject.extension.okhttp3.OkhttpComponent
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@BootstrapComponent(
    bootstrapDependencies = [OkhttpComponent::class],
    applicationModules = [MediumModule::class]
)
interface MediumComponent {
    val client: MediumClient

    fun newUser(): MediumUserComponent.Builder
}

@Subcomponent(modules = [MediumUserModule::class])
interface MediumUserComponent {
    val user: MediumCall<User>

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun username(username: String): Builder

        fun build(): MediumUserComponent
    }
}

@Module
class MediumUserModule {
    @Provides
    fun provideUserCall(
        username: String,
        client: MediumClient
    ): MediumCall<User> {
        return client.user(username)
    }
}
