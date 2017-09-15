package com.schibstedspain.leku;

import android.content.Context;
import android.os.IBinder;
import android.support.annotation.RestrictTo;
import android.view.inputmethod.InputMethodManager;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class SoftKeyboardManager {

  public static void closeKeyboard(Context context, IBinder windowToken) {
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(windowToken, 0);
  }
}
