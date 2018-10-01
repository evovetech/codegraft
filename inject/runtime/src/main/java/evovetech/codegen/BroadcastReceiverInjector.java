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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static dagger.android.AndroidInjection.inject;
import static net.bytebuddy.asm.Advice.Argument;
import static net.bytebuddy.asm.Advice.OnMethodEnter;
import static net.bytebuddy.asm.Advice.This;

/**
 * Injects core Android types.
 */
public final
class BroadcastReceiverInjector {
    private
    BroadcastReceiverInjector() {}

    @OnMethodEnter
    public static
    void onCreate(
            @This BroadcastReceiver receiver,
            @Argument(0) Context context,
            @Argument(1) Intent ignored
    ) {
        inject(receiver, context);
    }
}
