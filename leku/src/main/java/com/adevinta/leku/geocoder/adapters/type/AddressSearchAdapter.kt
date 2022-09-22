package com.adevinta.leku.geocoder.adapters.type

import android.location.Address
import com.adevinta.leku.LekuViewHolder
import com.adevinta.leku.geocoder.adapters.base.LekuSearchAdapter

abstract class AddressSearchAdapter<T : LekuViewHolder> : LekuSearchAdapter<T, Address>()
