package com.adevinta.leku.geocoder.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.adevinta.leku.LekuViewHolder
import com.adevinta.leku.R
import com.adevinta.leku.geocoder.adapters.type.AddressSearchAdapter
import com.adevinta.leku.getFullAddressString

class SearchViewHolder(
    val textView: TextView,
) : LekuViewHolder(textView)

class DefaultAddressAdapter(
    val context: Context,
) : AddressSearchAdapter<SearchViewHolder>() {
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
        if (items.isNotEmpty()) {
            holder.textView.text = items[position].getFullAddressString(context)
        }
    }
}
