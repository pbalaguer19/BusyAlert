package com.example.pau.busyalert.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.pau.busyalert.Interfaces.HerokuEndpointInterface;
import com.example.pau.busyalert.JavaClasses.ApiUtils;
import com.example.pau.busyalert.JavaClasses.HerokuLog;
import com.example.pau.busyalert.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener,
        DialogInterface.OnClickListener{

    private static final int LOGOUT = 2;
    private SharedPreferences sharedPreferences;
    private Set<String> phones;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;
    private boolean isSureToContinue = false;
    private String lastName = "";
    private String lastStatus = "";
    private boolean canAccessToNetwork;


    /**
     * FIREBASE
     **/
    private FirebaseAuth firebaseAuth;

    /**
     * HEROKU
     */
    private HerokuEndpointInterface apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        firebaseAuth = FirebaseAuth.getInstance();
        apiService = ApiUtils.getAPIService();
        checkNetworkState();
        initSettings();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Preference pref = findPreference(s);

        if(canAccessToNetwork){
            if (pref instanceof EditTextPreference) {
                EditTextPreference editTextPreference = (EditTextPreference) pref;
                if(!lastName.equals(editTextPreference.getText())){
                    this.lastName = editTextPreference.getText();
                    editTextPreference.setSummary(editTextPreference.getText());
                    setFirebase("username", editTextPreference.getText());
                }
            }else if (pref instanceof ListPreference) {
                if(s.equals("status")){
                    ListPreference listPreference = (ListPreference) pref;
                    String st = listPreference.getValue();
                    if(!lastStatus.equals(st)){
                        this.lastStatus = st;
                        listPreference.setSummary(st);
                        Toast.makeText(this, R.string.status_changed,Toast.LENGTH_SHORT).show();
                        setFirebase("status" , st);
                        setStatus(st);
                    }
                }else if(s.equals("networkList")){
                    setFirebase("network" ,((ListPreference) pref).getValue());
                    Toast.makeText(this, ((ListPreference) pref).getValue(),Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            new AlertDialog.Builder(this)
                    .setMessage(R.string.network_settings)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, null)
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals("logout")){
            new AlertDialog.Builder(this)
                    .setMessage(R.string.exit)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getUserInfo("USER_LOGGED_OUT");
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }else if(preference.getKey().equals("update")){
            checkNetwork();
            if(this.isSureToContinue) updateContacts();
        }else if(preference.getKey().equals("delete")){
            new AlertDialog.Builder(this)
                    .setMessage(R.string.delete_sure)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteAccount();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }else if(preference.getKey().equals("historic")) {
            Intent intent = new Intent(this, HistoricActivity.class);
            startActivity(intent);
            finish();
        }

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

        Preference update = findPreference("update");
        update.setOnPreferenceClickListener(this);

        Preference delete = findPreference("delete");
        delete.setOnPreferenceClickListener(this);

        Preference historic = findPreference("historic");
        historic.setOnPreferenceClickListener(this);
    }

    private void checkNetworkState(){
        final boolean wifiConnected, mobileConnected;

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sPref = sharedPrefs.getString("networkList", "WIFI");
        canAccessToNetwork = (((sPref.equals("ANY")) &&
                (wifiConnected || mobileConnected))
                || ((sPref.equals("WIFI")) && (wifiConnected)));
    }

    /* This is for the Update Contacts Option */
    private void checkNetwork(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo.getType() != ConnectivityManager.TYPE_WIFI){
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_wifi_continue)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, this)
                    .setNegativeButton(R.string.no, null)
                    .show();
        }else{
            this.isSureToContinue = true;
        }
    }

    private void updateContacts(){
        getPermissionToReadUserContacts();
        getContacts();
        saveUserContacts();
        Toast.makeText(this, R.string.contacts_updated,Toast.LENGTH_SHORT).show();
    }

    private void getPermissionToReadUserContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {}
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS_PERMISSIONS_REQUEST);
        }
    }

    private void getContacts() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor people = this.getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        phones = new HashSet<>();
        if (people.moveToFirst()) {
            do {
                String phone = people.getString(indexName).replace(" ", "");
                if(phone != null && !phone.equals("") &&
                        (phone.length() == 9 || phone.length() == 12) &&
                        phone.contains("6")){
                    while (!phone.startsWith("6")){
                        phone = phone.substring(1);
                    }
                    phones.add(phone);
                }
            } while (people.moveToNext());
        }
    }


    private void saveUserContacts(){
        for (String phone: phones){
            if(phone.length() == 9)
                saveContact(phone);
        }
    }

    private void saveContact(String contact){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        final String phone = contact;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users-phone");

        ref.child(contact).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users-contacts");
                    ref.child(uid).child(phone).setValue(true);
                }
                else {
                    // The contact has not the app
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //ONCLICK DIALOG INTERFACE//
    @Override
    public void onClick(DialogInterface dialog, int which) {
        this.isSureToContinue = true;
    }

    private void setFirebase(String child, String type){
        String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).child(child).setValue(type);
    }

    private void setStatus(String status) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude;
        double latitude;
        if(location == null){
            latitude = 0.0;
            longitude = 0.0;
        }else{
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        String extra = "Lat: " + latitude + " | Long: " + longitude;
        String action;
        if(status.equals("Busy"))
            action = "STATUS_BUSY";
        else
            action = "STATUS_AVAILABLE";

        apiService.createLog(uid, action, extra).enqueue(new Callback<HerokuLog>() {
            @Override
            public void onResponse(Call<HerokuLog> call, Response<HerokuLog> response) {
            }

            @Override
            public void onFailure(Call<HerokuLog> call, Throwable t) {

            }
        });

    }

    public void getUserInfo(final String action){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email = "", phone = "";
                    for (DataSnapshot dSnap: snapshot.getChildren()) {
                        if(dSnap.getKey().equals("email")) {
                            email = dSnap.getValue(String.class);
                        }else if(dSnap.getKey().equals("phone")) {
                            phone = dSnap.getValue(String.class);
                        }
                    }
                    String extra = "Email: " + email + " | Phone: " + phone;
                    apiService.createLog(uid, action, extra).enqueue(new Callback<HerokuLog>() {
                        @Override
                        public void onResponse(Call<HerokuLog> call, Response<HerokuLog> response) {
                        }

                        @Override
                        public void onFailure(Call<HerokuLog> call, Throwable t) {

                        }
                    });

                    setResult(LOGOUT);
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void deleteAcc(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential("user@example.com", "password1234");

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Account deleted",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                });

        setResult(LOGOUT);
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void deleteAccount(){
        final String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email = "", phone = "";
                    for (DataSnapshot dSnap: snapshot.getChildren()) {
                        if(dSnap.getKey().equals("email")) {
                            email = dSnap.getValue(String.class);
                        }else if(dSnap.getKey().equals("phone")) {
                            phone = dSnap.getValue(String.class);
                        }
                    }
                    String extra = "Email: " + email + " | Phone: " + phone;
                    apiService.createLog(uid, "USER_UNSUBSCRIBED", extra).enqueue(new Callback<HerokuLog>() {
                        @Override
                        public void onResponse(Call<HerokuLog> call, Response<HerokuLog> response) {
                        }

                        @Override
                        public void onFailure(Call<HerokuLog> call, Throwable t) {

                        }
                    });

                    apiService.deleteLogs(uid).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });

                    deleteAcc();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
