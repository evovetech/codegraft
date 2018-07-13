/*
 * Copyright 2018 evove.tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package evovetech.finance.plaid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjector
import evovetech.finance.plaid.ui.plaid.PlaidFragment
import sourcerer.inject.AndroidInject
import sourcerer.inject.android.HasSupportFragmentInjector
import sourcerer.inject.android.SupportFragment
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
