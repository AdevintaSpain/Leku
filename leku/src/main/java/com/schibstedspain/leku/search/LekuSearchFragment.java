package com.schibstedspain.leku.search;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.schibstedspain.leku.LocationPickerActivity;
import com.schibstedspain.leku.R;
import java.util.List;

public class LekuSearchFragment extends Fragment {

  public static LekuSearchFragment newInstance() {
    return new LekuSearchFragment();
  }

  private static final String LAST_LOCATION_QUERY = "last_location_query";
  private static final int MIN_CHARACTERS = 2;
  private static final int REQUEST_PLACE_PICKER = 6655;
  private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

  private Toolbar toolbar;
  private EditText searchView;
  private ImageView clearSearchButton;
  private TextWatcher textWatcher;
  private MenuItem searchOption;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.toolbar_search, null, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    bindViews(view);
    setupToolbar();
    setUpClearListener();
    setUpSearchViewListeners();
  }

  private void bindViews(View view) {
    toolbar = (Toolbar) view.findViewById(R.id.map_search_toolbar);
    searchView = (EditText) view.findViewById(R.id.leku_search);
    clearSearchButton = (ImageView) view.findViewById(R.id.leku_clear_search_image);
  }

  private void setupToolbar() {
    toolbar.inflateMenu(R.menu.toolbar_menu);
    searchOption = toolbar.getMenu().findItem(R.id.action_voice);
    toolbar.setOnMenuItemClickListener(this::onToolbarMenuItemSelected);
  }

  private boolean onToolbarMenuItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_voice) {
      onVoiceItemSelected();
      return true;
    }
    return false;
  }

  private void onVoiceItemSelected() {
    if (searchView.getText().toString().isEmpty()) {
      startVoiceRecognitionActivity();
    } else {
      retrieveLocationFrom(searchView.getText().toString());
      closeKeyboard();
    }
  }

  private void startVoiceRecognitionActivity() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search_promp));
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getString(R.string.voice_search_extra_language));

    if (checkPlayServices()) {
      try {
        startActivityForResult(intent, REQUEST_PLACE_PICKER);
      } catch (ActivityNotFoundException e) {
        Log.d(LocationPickerActivity.class.getName(), e.getMessage());
      }
    }
  }

  private boolean checkPlayServices() {
    GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
    int result = googleAPI.isGooglePlayServicesAvailable(getContext());
    if (result != ConnectionResult.SUCCESS) {
      if (googleAPI.isUserResolvableError(result)) {
        googleAPI.getErrorDialog(getActivity(), result, CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
      }
      return false;
    }
    return true;
  }

  private void setUpClearListener() {
    clearSearchButton.setOnClickListener(view -> searchView.setText(""));
  }

  private void setUpSearchViewListeners() {
    searchView.setOnEditorActionListener((v, actionId, event) -> {
      boolean handled = false;
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        retrieveLocationFrom(v.getText().toString());
        closeKeyboard();
        handled = true;
      }
      return handled;
    });
    textWatcher = getSearchTextWatcher();
    searchView.addTextChangedListener(textWatcher);
  }

  private void closeKeyboard() {
    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
  }

  @Override
  public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    if (savedInstanceState != null) {
      String lastQuery = savedInstanceState.getString(LAST_LOCATION_QUERY, "");
      if (!"".equals(lastQuery)) {
        retrieveLocationFrom(lastQuery);
      }
    }
  }

  private void retrieveLocationFrom(String query) {
    retrieveLocationFrom(query, true);
  }

  private void retrieveLocationFrom(String query, boolean debounce) {
    // TODO
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(LAST_LOCATION_QUERY, String.valueOf(searchView.getText()));
  }

  @Override
  public void onDestroy() {
    if (searchView != null && textWatcher != null) {
      searchView.removeTextChangedListener(textWatcher);
    }
    super.onDestroy();
  }

  private TextWatcher getSearchTextWatcher() {
    return new LekuSearchTextWatcher() {
      @Override
      public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
        if ("".equals(charSequence.toString())) {
          clearSearchResults();
          showLocationInfoLayout();
          if (clearSearchButton != null) {
            clearSearchButton.setVisibility(View.INVISIBLE);
          }

          if (searchOption != null) {
            searchOption.setIcon(R.drawable.ic_mic);
          }
        } else {
          if (charSequence.length() > MIN_CHARACTERS && after > count) {
            retrieveLocationFrom(charSequence.toString(), true);
          }
          if (clearSearchButton != null) {
            clearSearchButton.setVisibility(View.VISIBLE);
          }
          if (searchOption != null) {
            searchOption.setIcon(R.drawable.ic_search);
          }
        }
      }
    };
  }

  private void clearSearchResults() {
    // TODO adapter.clear();
    // TODO adapter.notifyDataSetChanged();
  }

  private void showLocationInfoLayout() {
    // TODO view.showLocationInfoLayout()
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_PLACE_PICKER:
        if (resultCode == Activity.RESULT_OK) {
          List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          retrieveLocationFrom(matches.get(0));
        }
        break;
      default:
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private class LekuSearchTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
  }
}
