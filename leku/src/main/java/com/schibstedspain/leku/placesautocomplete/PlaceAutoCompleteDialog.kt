package com.schibstedspain.leku.placesautocomplete


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.schibstedspain.leku.R
import com.schibstedspain.leku.placesautocomplete.adapter.PlacesAutoCompleteAdapter
import kotlinx.android.synthetic.main.leku_dialog_autocomplete.view.*


/**
 * Created by Mala Ruparel on 02/10/20.
 */

class PlaceAutoCompleteDialog() : DialogFragment(), TextWatcher {

    lateinit var autoComplete: EditText
    lateinit var rvListPlace: RecyclerView
    lateinit var btnCancel: AppCompatButton
    lateinit var locationListner: PlacesAutoCompleteAdapter.LocationListner
    private lateinit var placesAutoCompleteAdapter: PlacesAutoCompleteAdapter;
    override fun onResume() {
        super.onResume()
        val window = dialog!!.window
        window?.setGravity(Gravity.CENTER)

    }

    fun setListner(listner: PlacesAutoCompleteAdapter.LocationListner) {
        this.locationListner = listner
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        dialog!!.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCanceledOnTouchOutside(true)
       // dialog!!.window?.attributes?.windowAnimations = R.style.DialogAnimation
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.leku_dialog_autocomplete, null)
        val placesApi = PlaceAPI.Builder()
            .apiKey("")

            .build(requireContext())
        builder.setView(view)
        autoComplete = view.searchView
        btnCancel = view.cancel

        autoComplete.addTextChangedListener(this)

        autoComplete.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (event.getRawX() >= autoComplete.getRight() - autoComplete.getTotalPaddingRight()) {
                    autoComplete.setText("")
                    return true

                }
                return false
            }
        })
        rvListPlace = view.rvList
        showSearchBar(placesApi)
        view.cancel?.setOnClickListener {
            dismiss()

        }



        return builder.create()
    }

    private fun showSearchBar(placesApi: PlaceAPI) {

        placesAutoCompleteAdapter = PlacesAutoCompleteAdapter(
            requireContext(),
            placesApi!!,
            locationListner
        )


        rvListPlace?.adapter = placesAutoCompleteAdapter


    }


    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        isClearIconVisible(s)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        isClearIconVisible(s)
    }

    override fun afterTextChanged(s: Editable) {
        isClearIconVisible(s)
        if (::placesAutoCompleteAdapter.isInitialized) {

            placesAutoCompleteAdapter.filter?.filter(s);
        }
    }

    private fun isClearIconVisible(s: CharSequence) {
        if (s.isNotEmpty()) {
            autoComplete.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.leku_ic_search_24,
                0,
                R.drawable.leku_ic_cancel_round,
                0
            );
        } else
            autoComplete.setCompoundDrawablesWithIntrinsicBounds(R.drawable.leku_ic_search_24, 0, 0, 0);
    }

}
