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

package sourcerer

import dagger.Module
import dagger.Provides
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Options
import sourcerer.processor.log
import javax.annotation.processing.Filer
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@Module
class EnvModule {
    @Provides
    fun providesTypes(env: ProcessingEnv): Types {
        return env.typeUtils
    }

    @Provides
    fun providesElements(env: ProcessingEnv): Elements {
        return env.elementUtils
    }

    @Provides
    fun providesOptions(env: ProcessingEnv): Options {
        return env.options.apply {
            env.parent.messager.log("options = $this")
        }
    }

    @Provides
    fun providesFiler(env: ProcessingEnv): Filer {
        return env.filer
    }
}
