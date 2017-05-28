package com.schibstedspain.leku.search;

import android.text.Editable;
import android.text.TextWatcher;

abstract class LekuSearchTextWatcher implements TextWatcher {

  private int minCharacters;

  LekuSearchTextWatcher(int minCharacters) {
    this.minCharacters = minCharacters;
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override
  public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
    if ("".equals(charSequence.toString())) {
      onEmptyText();
    } else if (charSequence.length() > minCharacters && after > count) {
      onText(charSequence);
    }
  }

  abstract void onEmptyText();

  abstract void onText(CharSequence charSequence);

  @Override
  public void afterTextChanged(Editable s) {

  }
}