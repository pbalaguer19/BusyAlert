package com.example.pau.busyalert;

import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;

public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sharedPreferences.getString("speed_key", "20");
        sharedPreferences.getString("username_key", "John Smith");

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        Preference pref = findPreference("username_key");
        EditTextPreference editTextPreference = (EditTextPreference) pref;
        editTextPreference.setSummary(editTextPreference.getText());

        pref = findPreference("status");
        ListPreference listPreference = (ListPreference) pref;
        listPreference.setSummary(listPreference.getValue());
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Preference pref = findPreference("username_key");

        if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) pref;
            editTextPreference.setSummary(editTextPreference.getText());
        }

        pref = findPreference("status");
        if (pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            listPreference.setSummary(listPreference.getValue());
        }
    }
}
