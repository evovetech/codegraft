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

package evovetech.blog.medium

import codegraft.inject.android.AndroidApplication
import codegraft.inject.extension.crashlytics.CrashesBootstrapModule
import io.fabric.sdk.android.Fabric
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.notNull
import org.mockito.Mock
import org.mockito.Mockito.`when`
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

        val bootstrap = bootstrap {
            fabricBuilderFunction1 { fabric }
            crashesBootstrapModule(crashesBootstrapModule)
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
