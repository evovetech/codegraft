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

package evovetech.codegen;

import android.content.Context;
import android.support.v4.app.Fragment;
import dagger.android.support.AndroidSupportInjection;
import net.bytebuddy.asm.Advice;

import static net.bytebuddy.asm.Advice.*;

/**
 * Injects core Android types.
 */
public final
class SupportFragmentInjector {
    private
    SupportFragmentInjector() {}

    @OnMethodEnter
    public static
    void onAttach(
            @This Fragment fragment,
            @Argument(0) Context ignored
    ) {
        AndroidSupportInjection.inject(fragment);
    }
}
