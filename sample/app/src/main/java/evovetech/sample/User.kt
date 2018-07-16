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

package evovetech.sample

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import javax.inject.Inject

open
class User : RealmObject() {
    @PrimaryKey
    var email: String? = null
    var firstName: String? = null
    var lastName: String? = null

    class Manager
    @Inject constructor(
        val realm: Realm
    ) {
        @Throws(Throwable::class)
        inline
        fun getOrCreate(email: String, init: User.() -> Unit): User {
            return realm.getOrCreate({
                equalTo("email", email)
                email
            }, init)
        }
    }
}
