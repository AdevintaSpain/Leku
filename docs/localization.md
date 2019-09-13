If you would like to add more language translations the only thing you have to do is:

1. Crate a new strings resource folder and file for your language like "/values-ru".
2. Add all text translations for those strings:

```xml
<string name="leku_title_activity_location_picker">Location Picker</string>
<string name="leku_load_location_error">Something went wrong. Please try again.</string>
<string name="leku_no_search_results">There are no results for your search</string>
<string name="leku_unknown_location">unknown location</string>
<string name="leku_voice_search_promp">Search by voice…</string>
<string name="leku_voice_search_extra_language">en-EN</string>
<string name="leku_toolbar_action_voice_title">Voice</string>
<string name="leku_search_hint">Search</string>
```

Note that you have the **voice_search_extra_language** that is used for the language of the voice recognition.
Replace it with the allowed voice recognition locale for your language.

I encourage you to add these languages to this component, please fork this project and submit new languages with a PR. Thanks!


