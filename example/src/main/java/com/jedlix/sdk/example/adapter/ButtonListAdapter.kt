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
import androidx.recyclerview.widget.RecyclerView
import com.jedlix.sdk.example.databinding.ViewButtonRowBinding
import com.jedlix.sdk.example.model.Button

class ButtonListAdapter : RecyclerView.Adapter<ButtonListAdapter.ButtonViewHolder>() {

    companion object {
        @BindingAdapter("modelButtons")
        @JvmStatic
        fun bindModelButtons(recyclerView: RecyclerView, buttons: List<Button>?) {
            (recyclerView.adapter as? ButtonListAdapter)?.buttons = buttons ?: emptyList()
        }
    }

    class ButtonViewHolder(val binding: ViewButtonRowBinding) : RecyclerView.ViewHolder(binding.root)

    private var buttons: List<Button> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = buttons.size

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.binding.button.apply {
            val modelButton = buttons.getOrNull(position)
            text = modelButton?.title
            setOnClickListener { modelButton?.action?.invoke() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ButtonViewHolder(
        ViewButtonRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
}
