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

package evovetech.blog.medium

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import codegraft.inject.AndroidInject
import codegraft.inject.android.HasSupportFragmentInjector
import codegraft.inject.android.SupportFragment
import dagger.android.AndroidInjector
import evovetech.blog.medium.ui.medium.MediumFragment
import javax.inject.Inject

@AndroidInject
class MediumActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject override lateinit
    var supportFragmentInjector: AndroidInjector<SupportFragment>

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.medium_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MediumFragment.newInstance())
                    .commitNow()
        }
    }
}
