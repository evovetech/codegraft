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

package sourcerer.inject

import android.app.Application
import android.util.Log

// Content Provider
class BootstrapProvider : EmptyContentProvider() {
    override
    fun onCreate(): Boolean {
        val TAG = "BootstrapProvider"
        val app = context as Application
        when (app) {
            is BootApplication<*> -> {
                Log.d(TAG, "Bootstrapping!!")
                app.bootstrap.initialize()
            }
            else -> {
                Log.d(TAG, "NO Bootstraps :(")
            }
        }
        return true
    }
}
