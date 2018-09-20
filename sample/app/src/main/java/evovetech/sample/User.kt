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
