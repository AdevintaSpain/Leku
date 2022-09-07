package com.adevinta.leku

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

class SearchViewHolder(
    val textView: TextView,
) : LekuViewHolder(textView)

class DefaultAdapter(
    val context: Context,
) : LocationSearchAdapter<SearchViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.leku_search_list_item,
                parent,
                false
            ) as TextView

        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (locations.isNotEmpty()) {
            holder.textView.text = locations[position].getFullAddressString(context)
        }
    }
}
