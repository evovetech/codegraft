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

package evovetech.finance.plaid.ui.plaid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import evovetech.finance.plaid.R
import sourcerer.inject.AndroidInject
import sourcerer.inject.android.ViewModelInstanceProvider
import javax.inject.Inject

@AndroidInject
class PlaidFragment : Fragment() {
    @Inject lateinit
    var viewModelProvider: ViewModelInstanceProvider<PlaidFragment>

    private
    val viewModel: PlaidViewModel
        get() = viewModelProvider.get()

    override
    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.plaid_fragment, container, false)
    }

    override
    fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.apply {
            Log.d("PlaidFragment", "plaid view model = $this, plaid client = $client}")
        }
        // TODO: Use the ViewModel
    }

    companion object {
        fun newInstance() = PlaidFragment()
    }
}
