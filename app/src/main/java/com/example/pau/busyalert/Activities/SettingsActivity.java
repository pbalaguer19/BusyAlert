package com.example.pau.busyalert.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.pau.busyalert.R;

public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final int LOGOUT = 2;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        initSettings();
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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.exit)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setResult(LOGOUT);
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
        return true;
    }

    private void initSettings(){
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        Preference pref = findPreference("username_key");
        EditTextPreference editTextPreference = (EditTextPreference) pref;
        String username = editTextPreference.getText();

        if (username == null){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username_key", "John Smith");
            editor.apply();
            username = "John Smith";
            editTextPreference.setText(username);
        }
        editTextPreference.setSummary(username);

        pref = findPreference("status");
        ListPreference listPreference = (ListPreference) pref;
        if(listPreference.getValue() == null) listPreference.setValueIndex(0);
        listPreference.setSummary(listPreference.getValue());

        pref = findPreference("networkList");
        listPreference = (ListPreference) pref;
        if(listPreference.getValue() == null) listPreference.setValueIndex(1);

        Preference button = findPreference("logout");
        button.setOnPreferenceClickListener(this);
    }
}
