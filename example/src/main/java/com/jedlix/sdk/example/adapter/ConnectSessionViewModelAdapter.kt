/*
 * Copyright 2022 Jedlix B.V. The Netherlands
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.jedlix.sdk.example.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.jedlix.sdk.example.databinding.ViewConnectSessionViewModelRowBinding
import com.jedlix.sdk.example.viewModel.ConnectSessionViewModel

class ConnectSessionViewModelAdapter(val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<ConnectSessionViewModelAdapter.ViewHolder>() {

    companion object {
        @BindingAdapter("connectSessionViewModels")
        @JvmStatic
        fun bindModelButtons(recyclerView: RecyclerView, connectSessionViewModels: List<ConnectSessionViewModel>?) {
            (recyclerView.adapter as? ConnectSessionViewModelAdapter)?.connectSessionViewModels = connectSessionViewModels ?: emptyList()
        }
    }

    class ViewHolder(val binding: ViewConnectSessionViewModelRowBinding) : RecyclerView.ViewHolder(binding.root)

    private var connectSessionViewModels: List<ConnectSessionViewModel> = emptyList()
    @SuppressLint("NotifyDataSetChanged")
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = connectSessionViewModels.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            viewModel = connectSessionViewModels[position]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ViewConnectSessionViewModelRowBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            lifecycleOwner = this@ConnectSessionViewModelAdapter.lifecycleOwner
            buttons.adapter = ButtonListAdapter()
        }
    )
}