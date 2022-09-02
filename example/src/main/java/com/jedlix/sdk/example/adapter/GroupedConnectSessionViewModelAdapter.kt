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
import com.jedlix.sdk.example.databinding.ViewGroupedConnectSessionViewModelRowBinding
import com.jedlix.sdk.example.viewModel.GroupedConnectSessionViewModel

class GroupedConnectSessionViewModelAdapter(private val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<GroupedConnectSessionViewModelAdapter.ViewHolder>() {

    companion object {
        @BindingAdapter("groupedConnectSessionViewModels")
        @JvmStatic
        fun bindModelButtons(recyclerView: RecyclerView, connectSessionViewModels: List<GroupedConnectSessionViewModel>?) {
            (recyclerView.adapter as? GroupedConnectSessionViewModelAdapter)?.groupedConnectSessionViewModels = connectSessionViewModels ?: emptyList()
        }
    }

    class ViewHolder(val binding: ViewGroupedConnectSessionViewModelRowBinding) : RecyclerView.ViewHolder(binding.root)

    private var groupedConnectSessionViewModels: List<GroupedConnectSessionViewModel> = emptyList()
    @SuppressLint("NotifyDataSetChanged")
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = groupedConnectSessionViewModels.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            viewModel = groupedConnectSessionViewModels[position]

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ViewGroupedConnectSessionViewModelRowBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            lifecycleOwner = this@GroupedConnectSessionViewModelAdapter.lifecycleOwner
            connectSessions.adapter = ConnectSessionViewModelAdapter(this@GroupedConnectSessionViewModelAdapter.lifecycleOwner)
        }
    )
}