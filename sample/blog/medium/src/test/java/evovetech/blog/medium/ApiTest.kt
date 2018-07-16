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

import evovetech.sample.crashes.CrashesBootstrapModule
import evovetech.sample.db.realm.RealmBootstrapModule
import io.fabric.sdk.android.Fabric
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.notNull
import org.mockito.Mock
import org.mockito.Mockito.`when`
import sourcerer.inject.android.AndroidApplication
import kotlin.test.assertNotNull

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ApiTest : BaseTest() {
    private lateinit
    var component: AppComponent

    @Mock lateinit
    var crashesBootstrapModule: CrashesBootstrapModule

    @Mock lateinit
    var fabric: Fabric

    @Mock lateinit
    var realmBootstrapModule: RealmBootstrapModule

    @Mock lateinit
    var realm: RealmConfiguration

    @Before
    fun setup() {
        val app = this.app
        `when`(
            crashesBootstrapModule.provideFabric(
                notNull<AndroidApplication>().let { app },
                any()
            )
        ).thenAnswer {
            fabric
        }

        `when`(
            realmBootstrapModule.provideRealmConfiguration(
                notNull<AndroidApplication>().let { app },
                any()
            )
        ).thenAnswer {
            realm
        }

        val bootstrap = bootstrap {
            fabricBuilderFunction1 { fabric }
            crashesBootstrapModule(crashesBootstrapModule)
            realmConfigurationBuilderFunction1 { realm }
            realmBootstrapModule(realmBootstrapModule)
            okHttpClientApplicationBuilderFunction2 { builder, _ ->
                builder.build()
            }
            app
        }
        component = bootstrap.component
    }

    @Test
    fun testComponent() {
        assertNotNull(component, "component must not be null!")
        println("testComponent success!")
    }

    @Test
    fun testClient() {
        val user = me()
        println("user=${user}")
    }

    @Test
    fun testUserPublications() {
        val client = component.mediumComponent.client
        val user = me()
        val response = user.publications(client).execute()
        assertNotNull(response, "response must not be null")
        println("response=$response")
        val msg: String = response.fold(success = { body ->
            "publications=${body?.data}"
        }, failure = { body ->
            "errors=${body?.string()}"
        })
        println(msg)
    }

    private
    fun me(): User {
        val client = component.mediumComponent.client
        assertNotNull(client, "client must not be null!")
        val response = client.user().execute()
        assertNotNull(response, "response must not be null")
        return response.fold(success = {
            it?.data!!
        }, failure = {
            throw IllegalArgumentException("error = ${it?.string()}")
        })
    }
}
