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

package evovetech.finance.plaid.ui.plaid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import codegraft.inject.AndroidInject
import codegraft.inject.android.ViewModelInstanceProvider
import codegraft.inject.android.delegate
import evovetech.finance.plaid.R
import javax.inject.Inject

@AndroidInject
class PlaidFragment : Fragment() {
    @Inject lateinit
    var viewModels: ViewModelInstanceProvider

    private
    val viewModel: PlaidViewModel by ::viewModels.delegate()

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
