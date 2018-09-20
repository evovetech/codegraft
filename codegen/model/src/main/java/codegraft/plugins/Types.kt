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

package codegraft.plugins

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Multimap

typealias MutablePlugins = MutableSet<GeneratePluginBindingsDescriptor>
typealias Plugins = ImmutableSet<GeneratePluginBindingsDescriptor>

typealias MutableModules = Multimap<GeneratePluginBindingsDescriptor, GeneratePluginBindingsModuleDescriptor>
typealias Modules = ImmutableMultimap<GeneratePluginBindingsDescriptor, GeneratePluginBindingsModuleDescriptor>
typealias ModulesBuilder = ImmutableMultimap.Builder<GeneratePluginBindingsDescriptor, GeneratePluginBindingsModuleDescriptor>
