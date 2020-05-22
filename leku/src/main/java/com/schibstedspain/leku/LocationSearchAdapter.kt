package com.schibstedspain.leku

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationSearchAdapter(
        private val locations: MutableList<String>?,
        private val clickListener: SearchItemClickListener
) : RecyclerView.Adapter<LocationSearchAdapter.SearchViewHolder>() {
    class SearchViewHolder(val textView: TextView, private val clickListener: SearchItemClickListener) : RecyclerView.ViewHolder(textView), View.OnClickListener {
        init {
            textView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener.onItemClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.leku_search_list_item, parent, false) as TextView

        return SearchViewHolder(textView, clickListener)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        if (!locations.isNullOrEmpty()) {
            holder.textView.text = locations[position]
        }
    }

    override fun getItemCount() = locations?.size ?: 0

    interface SearchItemClickListener {
        fun onItemClick(position: Int)
    }
}
