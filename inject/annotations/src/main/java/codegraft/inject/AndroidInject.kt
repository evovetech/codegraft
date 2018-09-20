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

package codegraft.inject

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FILE
import kotlin.reflect.KClass

/**
 * Created by layne on 2/26/18.
 */

@MustBeDocumented
@Target(
    CLASS,
    FILE
)
@Retention(
    BINARY
)
annotation
class AndroidInject(
    /**
     * Additional `@Module`-annotated classes from which this activity is
     * composed.
     */
    val includes: Array<KClass<*>> = []
)
