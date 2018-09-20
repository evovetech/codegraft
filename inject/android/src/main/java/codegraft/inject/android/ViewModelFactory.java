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

package codegraft.inject.android;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import codegraft.inject.ClassKeyProviderMapKt;

import javax.inject.Inject;

public
class ViewModelFactory
        implements ViewModelProvider.Factory
{
    private final ViewModels viewModels;

    @Inject
    ViewModelFactory(ViewModels viewModels) {
        this.viewModels = viewModels;
    }

    @NonNull @Override public
    <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        T viewModel = ClassKeyProviderMapKt.get(viewModels, modelClass);
        if (viewModel == null) {
            throw new NullPointerException("viewModel cannot be null for class: " + modelClass.getCanonicalName());
        }
        return viewModel;
    }
}
