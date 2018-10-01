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

package evovetech.gradle.transform

import android.content.Context
import android.content.Intent
import android.os.Bundle
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.matcher.ElementMatcher.Junction
import net.bytebuddy.matcher.ElementMatchers.named
import net.bytebuddy.matcher.ElementMatchers.returns
import net.bytebuddy.matcher.ElementMatchers.takesArguments

private
val booleanType = TypeDescription.ForLoadedType(Boolean::class.java)
        .asUnboxed()!!

fun activityOnCreate(): Junction<MethodDescription> = named<MethodDescription>("onCreate")
        .and<MethodDescription>(takesArguments(Bundle::class.java))
        .and<MethodDescription>(returns(TypeDescription.VOID))

fun fragmentOnAttach(): Junction<MethodDescription> = named<MethodDescription>("onAttach")
        .and<MethodDescription>(takesArguments(Context::class.java))
        .and<MethodDescription>(returns(TypeDescription.VOID))

fun serviceOnCreate(): Junction<MethodDescription> = named<MethodDescription>("onCreate")
        .and<MethodDescription>(takesArguments(0))
        .and<MethodDescription>(returns(TypeDescription.VOID))

fun broadcastOnReceive(): Junction<MethodDescription> = named<MethodDescription>("onReceive")
        .and<MethodDescription>(takesArguments(Context::class.java, Intent::class.java))
        .and<MethodDescription>(returns(TypeDescription.VOID))

fun contentProviderOnCreate(): Junction<MethodDescription> = named<MethodDescription>("onCreate")
        .and<MethodDescription>(takesArguments(0))
        .and<MethodDescription>(returns(booleanType))

