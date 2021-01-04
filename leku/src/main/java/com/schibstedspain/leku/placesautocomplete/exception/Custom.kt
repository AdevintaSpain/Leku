package com.schibstedspain.leku.placesautocomplete.exception

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatAutoCompleteTextView

/**
 * Created by Mala Ruparel on 29/12/20.
 */
class Custom @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {

    override fun showDropDown() {
        val displayFrame = Rect()
        getWindowVisibleDisplayFrame(displayFrame)
        val locationOnScreen = IntArray(2)
        getLocationOnScreen(locationOnScreen)
        val bottom = locationOnScreen[1] + height
        val availableHeightBelow: Int = displayFrame.bottom - bottom
        val r: Resources = resources
        val bottomHeight =
            Math.round(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    60F,
                    r.getDisplayMetrics()
                )
            )
                .toInt()
        val downHeight = availableHeightBelow - bottomHeight
        dropDownHeight = downHeight
        // super.showDropDown()
    }
}