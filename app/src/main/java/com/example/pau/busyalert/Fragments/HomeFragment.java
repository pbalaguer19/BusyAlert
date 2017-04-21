package com.example.pau.busyalert.Fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pau.busyalert.BroadcastReceivers.LocatBroadcastReciver;
import com.example.pau.busyalert.JavaClasses.User;
import com.example.pau.busyalert.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Button btnMonitoring, btnNotification, btnPremium;
    private TextView textView, statusText;
    private boolean monitorEnabled = false, notificationsEnabled = false;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private PendingIntent pendingIntent;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private SharedPreferences sharedPreferences;
    private boolean canAccessToNetwork;

    /**
     * FIREBASE
     **/
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private  DatabaseReference ref;
    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if (snapshot.exists()) {
                for (DataSnapshot dSnap: snapshot.getChildren()) {
                    if(dSnap.getKey().equals("username")) {
                        String value = dSnap.getValue(String.class);
                        textView.setText(value);
                        sharedPreferences.edit().putString("username_key", value).apply();
                    }else if(dSnap.getKey().equals("status")) {
                        String value = dSnap.getValue(String.class);
                        statusText.setText(value);
                        sharedPreferences.edit().putString("status", value).apply();
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        btnMonitoring = (Button) root.findViewById(R.id.btnMonitoring);
        btnNotification = (Button) root.findViewById(R.id.btnNotifications);
        btnPremium = (Button) root.findViewById(R.id.btnPremiumNotifications);
        textView = (TextView) root.findViewById(R.id.name);
        statusText = (TextView) root.findViewById(R.id.status);

        checkNetwork();
        if(canAccessToNetwork){
            getUserInfo();
            addUserListener();
        }else{
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.network_fail)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, null)
                    .setNegativeButton(R.string.no, null)
                    .show();
        }

        btnMonitoring.setOnClickListener(this);
        btnNotification.setOnClickListener(this);
        btnPremium.setOnClickListener(this);

        setUpNotificationsButton();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNetwork();
        if(canAccessToNetwork){
            getUserInfo();
            addUserListener();
        }else{
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.network_fail)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, null)
                    .setNegativeButton(R.string.no, null)
                    .show();
        }

        if(monitorEnabled) btnMonitoring.setText(R.string.btn_monitoring_stop);
        else btnMonitoring.setText(R.string.btn_monitoring);

        if(notificationsEnabled) btnNotification.setText(R.string.btn_notifications_stop);
        else btnNotification.setText(R.string.btn_notifications);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnMonitoring:
                monitoringHandler();
                break;
            case R.id.btnNotifications:
                notificationsHandler();
                break;
            case R.id.btnPremiumNotifications:
                registerForContextMenu(view);
                getActivity().openContextMenu(view);
                break;
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.premium_notifications_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.optMeeting:
                setStatus("Busy");
                break;
            case  R.id.optSleeping:
                setStatus("Busy");
                break;
            case R.id.optOther:
                setStatus("Busy");
                break;

        }
        return super.onContextItemSelected(item);
   }

    private void getUserInfo(){
        String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dSnap: snapshot.getChildren()) {
                        if(dSnap.getKey().equals("username")) {
                            String value = dSnap.getValue(String.class);
                            textView.setText(value);
                        }else if(dSnap.getKey().equals("status")) {
                            String value = dSnap.getValue(String.class);
                            statusText.setText(value);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addUserListener(){
        String uid = firebaseAuth.getCurrentUser().getUid();
        this.ref = FirebaseDatabase.getInstance().getReference("users");

        ref.child(uid).addValueEventListener(valueEventListener);
    }

    private void removeUserListener(){
        String uid = firebaseAuth.getCurrentUser().getUid();
       this.ref = FirebaseDatabase.getInstance().getReference("users");

        ref.child(uid).removeEventListener(valueEventListener);
    }



    private void monitoringHandler(){
        if(!monitorEnabled){
            Toast.makeText(getContext(), R.string.enable_monitoring,Toast.LENGTH_SHORT).show();
            monitorEnabled = true;
            startLocationPendingIntent();
            btnMonitoring.setText(R.string.btn_monitoring_stop);
        }else{
            Toast.makeText(getContext(), R.string.disable_monitoring,Toast.LENGTH_SHORT).show();
            monitorEnabled = false;
            stopLocationPendingIntent();
            btnMonitoring.setText(R.string.btn_monitoring);
        }
    }

    private void setUpNotificationsButton() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    notificationsEnabled = snapshot.child("notificationsEnabled").getValue(Boolean.class);
                    if(notificationsEnabled) btnNotification.setText(R.string.btn_notifications_stop);
                    else btnNotification.setText(R.string.btn_notifications);
                }
                else {}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void notificationsHandler(){
        if(!notificationsEnabled){
            Toast.makeText(getContext(), R.string.enable_notifications,Toast.LENGTH_SHORT).show();
            notificationsEnabled = true;
            btnNotification.setText(R.string.btn_notifications_stop);

        }else{
            Toast.makeText(getContext(), R.string.disable_notifications,Toast.LENGTH_SHORT).show();
            notificationsEnabled = false;
            btnNotification.setText(R.string.btn_notifications);
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).child("notificationsEnabled").setValue(notificationsEnabled);
    }

    private void setStatus(String status){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("status", status);
        editor.apply();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).child("status").setValue(status);
    }

    private void checkNetwork(){
        final boolean wifiConnected, mobileConnected;

        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sPref = sharedPrefs.getString("networkList", "WIFI");
        canAccessToNetwork = (((sPref.equals("ANY")) &&
                                (wifiConnected || mobileConnected))
                                || ((sPref.equals("WIFI")) && (wifiConnected)));
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //.addApi(ActivityRecognition.API).build();
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(getContext());
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(getActivity(), result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    protected void startLocationPendingIntent() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 321);
            }

            return;
        }
        Intent locationIntent = new Intent(getContext(),
                LocatBroadcastReciver.class);
        pendingIntent = PendingIntent.getBroadcast(getContext(),
                0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, pendingIntent);

    }

    protected void stopLocationPendingIntent() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, pendingIntent);
            //ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,pendingIntent);

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 321);
            }

            return;
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {}

    @Override
    public void onStop() {
        super.onStop();
        removeUserListener();
        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();

    }

    @Override
    public void onPause() {
        super.onPause();
        removeUserListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
}
