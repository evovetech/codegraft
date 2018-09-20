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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import codegraft.inject.AndroidInject
import codegraft.inject.android.HasSupportFragmentInjector
import codegraft.inject.android.SupportFragment
import dagger.android.AndroidInjector
import evovetech.finance.plaid.ui.plaid.PlaidFragment
import javax.inject.Inject

@AndroidInject
class PlaidActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject override lateinit
    var supportFragmentInjector: AndroidInjector<SupportFragment>

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plaid_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, PlaidFragment.newInstance())
                    .commitNow()
        }
    }
}
