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

package evovetech.finance.plaid

import android.app.IntentService
import android.content.Intent
import android.util.Log
import codegraft.inject.AndroidInject
import javax.inject.Inject

@AndroidInject
class PlaidIntentService : IntentService("PlaidService") {
    @Inject lateinit
    var client: PlaidClient

    override
    fun onCreate() {
        super.onCreate()
        Log.d("PlaidIntentService", "client -> $client")
    }

    override
    fun onHandleIntent(intent: Intent?) {
        Log.d("PlaidIntentService", "client -> $client")
    }
}
