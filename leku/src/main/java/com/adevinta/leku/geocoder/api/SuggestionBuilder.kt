package com.adevinta.leku.geocoder.api

import com.adevinta.leku.geocoder.PlaceSuggestion
import org.json.JSONException
import org.json.JSONObject

class SuggestionBuilder {

    @Throws(JSONException::class)
    fun parseResult(json: String): List<PlaceSuggestion> {
        val suggestions = ArrayList<PlaceSuggestion>()
        val root = JSONObject(json)
        val results = root.getJSONArray("predictions")
        for (i in 0 until results.length()) {
            suggestions.add(parseSuggestion(results.getJSONObject(i)))
        }
        return suggestions
    }

    @Throws(JSONException::class)
    private fun parseSuggestion(jsonObject: JSONObject): PlaceSuggestion {
        val description = jsonObject.getString("description")
        val placeId = jsonObject.getString("place_id")
        return PlaceSuggestion(description, placeId)
    }
}
