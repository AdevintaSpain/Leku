package com.adevinta.leku

import android.location.Address
import androidx.recyclerview.widget.RecyclerView

abstract class LocationSearchAdapter<T : LekuViewHolder> : RecyclerView.Adapter<T>() {
    var locations: List<Address> = emptyList()
    var onClick: (position: Int) -> Unit = {}

    override fun getItemCount() = locations.size

    override fun onBindViewHolder(holder: T, position: Int) {
        holder.itemView.setOnClickListener { onClick(position) }
    }
}
