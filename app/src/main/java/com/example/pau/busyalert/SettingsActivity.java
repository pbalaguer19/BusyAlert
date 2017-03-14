package com.example.pau.busyalert;

import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
        if(listPreference.getValue() == null) listPreference.setValueIndex(0);
        listPreference.setSummary(listPreference.getValue());

        pref = findPreference("networkList");
        listPreference = (ListPreference) pref;
        if(listPreference.getValue() == null) listPreference.setValueIndex(1);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Preference pref = findPreference(s);

        if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) pref;
            editTextPreference.setSummary(editTextPreference.getText());
        }else if (pref instanceof ListPreference) {
            if(s.equals("status")){
                ListPreference listPreference = (ListPreference) pref;
                listPreference.setSummary(listPreference.getValue());
                Toast.makeText(this, R.string.status_changed,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
