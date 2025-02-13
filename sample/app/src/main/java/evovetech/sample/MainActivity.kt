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

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import codegraft.inject.AndroidInject
import codegraft.inject.Plugins
import codegraft.inject.extension.okhttp3.Okhttp
import codegraft.inject.get
import evovetech.finance.plaid.PlaidActivity
//import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.message
import kotlinx.android.synthetic.main.activity_main.navigation
import javax.inject.Inject

@AndroidInject
class MainActivity : AppCompatActivity() {
    @Inject lateinit
    var plugins: Plugins

//    @Inject lateinit
//    var realm: Realm

//    @Inject lateinit
//    var userManager: User.Manager

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val client = plugins.get<Okhttp>()!!
        Log.d("tag", "client = $client")
        Toast.makeText(this, "client = $client", Toast.LENGTH_LONG)
                .show()
        startActivity(Intent(this, PlaidActivity::class.java))
    }

    override fun onDestroy() {
        // TODO: ugly having to do this twice
//        realm.close()
//        userManager.realm.close()
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
//                val user = userManager.getOrCreate("laynepenney@gmail.com") {
//                    firstName = "Layne"
//                    lastName = "Penney"
//                }
                val text = getString(R.string.title_dashboard)// + "\n\nUser: " + user.toString()
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
