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

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import evovetech.finance.plaid.PlaidActivity
import evovetech.sample.network.Client
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.message
import kotlinx.android.synthetic.main.activity_main.navigation
import codegraft.inject.AndroidInject
import codegraft.inject.Plugins
import codegraft.inject.get
import javax.inject.Inject

@AndroidInject
class MainActivity : AppCompatActivity() {
    @Inject lateinit
    var plugins: Plugins

    @Inject lateinit
    var realm: Realm

    @Inject lateinit
    var userManager: User.Manager

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val client = plugins.get<Client>()!!
        Log.d("tag", "client = $client")
        Toast.makeText(this, "client = $client", Toast.LENGTH_LONG)
                .show()
        startActivity(Intent(this, PlaidActivity::class.java))
    }

    override fun onDestroy() {
        // TODO: ugly having to do this twice
        realm.close()
        userManager.realm.close()
        super.onDestroy()
    }

    private
    val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                val user = userManager.getOrCreate("laynepenney@gmail.com") {
                    firstName = "Layne"
                    lastName = "Penney"
                }
                val text = getString(R.string.title_dashboard) + "\n\nUser: " + user.toString()
                message.text = text
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
