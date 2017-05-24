package com.schibstedspain.leku.search;

public interface LekuSearchCallback {
  void clearSearchResults();
  void showLocationInfoLayout();
  void retrieveLocationFrom(String query, boolean debounce);
}