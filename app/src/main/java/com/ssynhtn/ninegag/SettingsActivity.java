package com.ssynhtn.ninegag;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class SettingsActivity extends PreferenceActivity implements  Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_num_columns)));
    }

    private void bindPreferenceSummaryToValue(Preference preference){
        preference.setOnPreferenceChangeListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        String value = prefs.getString(preference.getKey(), "");
        onPreferenceChange(preference, value);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();

        if(preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            CharSequence displayValue = listPreference.getEntries()[index];
            listPreference.setSummary(displayValue);
        } else {
            preference.setSummary(stringValue);
        }

        return true;
    }

}
