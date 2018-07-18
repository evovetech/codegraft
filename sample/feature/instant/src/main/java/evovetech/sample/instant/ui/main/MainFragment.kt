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

package evovetech.sample.instant.ui.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import codegraft.inject.AndroidInject
import codegraft.inject.android.ViewModelInstanceProvider
import codegraft.inject.android.viewModelDelegate
import evovetech.sample.instant.R
import javax.inject.Inject

@AndroidInject
class MainFragment : Fragment() {
    @Inject lateinit
    var viewModelProvider: ViewModelInstanceProvider<MainFragment>

    private
    val viewModel: MainViewModel by viewModelDelegate(::viewModelProvider)

    override
    fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override
    fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.apply {
            Log.d("MainFragment", "viewModel=$this")
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}