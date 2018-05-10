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
import io.realm.RealmModel
import io.realm.RealmQuery
import io.realm.log.RealmLog

@Throws(Throwable::class)
inline
fun <reified T : RealmModel> Realm.getOrCreate(
    query: RealmQuery<out T>.() -> Any?,
    init: T.() -> Unit
): T {
    val w = where(T::class.java)
    val primaryKey = w.query()
    return w.findFirst()?.let { t ->
        //        t.init()
        t
    } ?: create(primaryKey, init)
}

@Throws(Throwable::class)
inline
fun <reified T : RealmModel> Realm.create(
    init: T.() -> Unit
): T {
    return create(primaryKey = null, init = init)
}

@Throws(Throwable::class)
inline
fun <reified T : RealmModel> Realm.create(
    primaryKey: Any?,
    init: T.() -> Unit
): T {
    return execTransaction {
        val obj = primaryKey?.let { createObject(T::class.java, it) }
                  ?: createObject(T::class.java)
        obj.init()
        return@execTransaction obj
    }
}

@Throws(Throwable::class)
inline
fun <T : Any?> Realm.execTransaction(
    f: Realm.() -> T
): T {
    beginTransaction()
    try {
        val ret = this.f()
        commitTransaction()
        return ret
    } catch (e: Throwable) {
        if (isInTransaction) {
            cancelTransaction()
        } else {
            RealmLog.warn("Could not cancel transaction, not currently in a transaction.")
        }
        throw e
    }
}
