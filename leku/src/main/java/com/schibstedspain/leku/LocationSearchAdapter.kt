package com.schibstedspain.leku

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class LocationSearchAdapter(private val locations: MutableList<String>?) :
        RecyclerView.Adapter<LocationSearchAdapter.SearchViewHolder>() {

    class SearchViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.leku_search_list_item, parent, false) as TextView

        return SearchViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        if (!locations.isNullOrEmpty()) {
            holder.textView.text = locations[position]
        }
    }

    override fun getItemCount() = locations?.size ?: 0
}