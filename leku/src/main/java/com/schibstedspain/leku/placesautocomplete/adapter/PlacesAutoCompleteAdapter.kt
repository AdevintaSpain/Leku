package com.schibstedspain.leku.placesautocomplete.adapter

import com.schibstedspain.leku.placesautocomplete.PlaceAPI

import com.schibstedspain.leku.placesautocomplete.model.Place
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schibstedspain.leku.R


/**
 * Created by MalaRuparel on 28/12/2020.
 */
class PlacesAutoCompleteAdapter(
        mContext: Context,
        val placesApi: PlaceAPI,
        var locationListner: LocationListner

) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    var address1: String = ""
    var address2: String = ""
    var resultList: ArrayList<Place> = ArrayList()

    interface LocationListner {
        fun dialogDismiss()
        fun dialogSave(place: PlaceAPI, place1: Place)
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    resultList = results.values as ArrayList<Place>
                    notifyDataSetChanged()
                } else
                    notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    resultList = placesApi.autocomplete(constraint.toString())!!
                    // resultList?.add(Place("-1", "footer"))
                    filterResults.values = resultList
                    filterResults.count = resultList!!.size
                }
                return filterResults
            }
        }
    }

    internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {


        var text1: TextView =
            view.findViewById(R.id.autocompleteText)
        var text2: TextView =
            view.findViewById(R.id.description)

        init {
            view.setOnClickListener(this)


        }

        override fun onClick(v: View?) {
            locationListner.dialogDismiss()
            locationListner?.dialogSave(placesApi, resultList[adapterPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leku_autocomplete_list_item, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        holder as ViewHolder
        if (!resultList.isNullOrEmpty()) {
            generateFinalAddress(resultList.get(position).description, holder)

            holder.text1?.visibility = View.VISIBLE
        } else {

            holder.text2?.visibility = View.GONE

        }
    }

    override fun getItemCount(): Int {

        return resultList.size

    }

    private fun generateFinalAddress(
        address: String,
        holder: ViewHolder
    ) {
        val string = address.split(",")
        address1 = ""
        address2 = ""


        if (string.size == 1) {
            address1 = string[0]
            address2 = string[0]
        }


        if (string.size > 1) {

            for ((index, value) in string.withIndex()) {
                if (index < 1) {
                    address1 = value
                } else {
                    address2 += value
                }
            }
        }
        holder.text1.text = address1
        holder.text2.text = address2
    }

}
