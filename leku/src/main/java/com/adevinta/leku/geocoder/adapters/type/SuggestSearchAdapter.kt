package com.adevinta.leku.geocoder.adapters.type

import com.adevinta.leku.LekuViewHolder
import com.adevinta.leku.geocoder.PlaceSuggestion
import com.adevinta.leku.geocoder.adapters.base.LekuSearchAdapter

abstract class SuggestSearchAdapter<T : LekuViewHolder> : LekuSearchAdapter<T, PlaceSuggestion>()
