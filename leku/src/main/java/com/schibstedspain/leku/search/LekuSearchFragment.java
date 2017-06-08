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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.schibstedspain.leku.LocationPicker;
import com.schibstedspain.leku.NullView;
import com.schibstedspain.leku.R;
import com.schibstedspain.leku.SoftKeyboardManager;
import com.schibstedspain.leku.tracker.TrackEvents;
import java.util.List;

public class LekuSearchFragment extends Fragment {

  private LekuSearchCallback lekuSearchCallback;

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
    setupClearListener();
    setupSearchViewListeners();
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

  public void clearFilter() {
    searchView.setText("");
    SoftKeyboardManager.closeKeyboard(getContext(), searchView.getWindowToken());
  }

  private void onVoiceItemSelected() {
    if (searchView.getText().toString().isEmpty()) {
      startVoiceRecognitionActivity();
    } else {
      retrieveLocationFrom(searchView.getText().toString());
      SoftKeyboardManager.closeKeyboard(getContext(), searchView.getWindowToken());
    }
  }

  private void startVoiceRecognitionActivity() {
    if (checkPlayServices()) {
      Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search_promp));
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getString(R.string.voice_search_extra_language));

      try {
        startActivityForResult(intent, REQUEST_PLACE_PICKER);
      } catch (ActivityNotFoundException e) {
        track(TrackEvents.noVoiceRecognition);
      }
    }
  }

  protected void track(TrackEvents event) {
    LocationPicker.getTracker().onEventTracked(event);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof LekuSearchCallback) {
      lekuSearchCallback = (LekuSearchCallback) context;
    } else {
      lekuSearchCallback = NullView.createFor(LekuSearchCallback.class);
    }
  }

  @Override
  public void onDetach() {
    lekuSearchCallback = NullView.createFor(LekuSearchCallback.class);
    super.onDetach();
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

  private void setupClearListener() {
    clearSearchButton.setOnClickListener(view -> searchView.setText(""));
  }

  private void setupSearchViewListeners() {
    searchView.setOnEditorActionListener((v, actionId, event) -> {
      boolean handled = false;
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        retrieveLocationFrom(v.getText().toString());
        SoftKeyboardManager.closeKeyboard(getContext(), searchView.getWindowToken());
        handled = true;
      }
      return handled;
    });
    textWatcher = getSearchTextWatcher();
    searchView.addTextChangedListener(textWatcher);
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
    retrieveLocationFrom(query, false);
  }

  private void retrieveLocationFrom(String query, boolean debounce) {
    lekuSearchCallback.retrieveLocationFrom(query, debounce);
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
    return new LekuSearchTextWatcher(MIN_CHARACTERS) {
      @Override
      void onEmptyText() {
        if (clearSearchButton != null) {
          clearSearchButton.setVisibility(View.INVISIBLE);
        }
        if (searchOption != null) {
          searchOption.setIcon(R.drawable.ic_mic);
        }
        clearSearchResults();
        showLocationInfoLayout();
      }

      @Override
      void onText(CharSequence charSequence) {
        if (clearSearchButton != null) {
          clearSearchButton.setVisibility(View.VISIBLE);
        }
        if (searchOption != null) {
          searchOption.setIcon(R.drawable.ic_search);
        }
        retrieveLocationFrom(charSequence.toString(), true);
      }
    };
  }

  private void clearSearchResults() {
    lekuSearchCallback.clearSearchResults();
  }

  private void showLocationInfoLayout() {
    lekuSearchCallback.showLocationInfoLayout();
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
}
