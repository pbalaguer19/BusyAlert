package com.example.pau.busyalert.Fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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
import com.example.pau.busyalert.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


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


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String name = sharedPreferences.getString("username_key", "John Smith");
        String status = sharedPreferences.getString("status", "Available");
        textView.setText(name);
        statusText.setText(status);

        btnMonitoring.setOnClickListener(this);
        btnNotification.setOnClickListener(this);
        btnPremium.setOnClickListener(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String name = sharedPreferences.getString("username_key", "John Smith");
        String status = sharedPreferences.getString("status", "Available");
        textView.setText(name);
        statusText.setText(status);
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
                Toast.makeText(getContext(), R.string.meeting_toast,Toast.LENGTH_SHORT).show();
                break;
            case  R.id.optSleeping:
                Toast.makeText(getContext(), R.string.sleeping_toast,Toast.LENGTH_SHORT).show();
                break;
            case R.id.optOther:
                Toast.makeText(getContext(), R.string.other_toast,Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onContextItemSelected(item);
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

    private void notificationsHandler(){
        if(!notificationsEnabled){
            Toast.makeText(getContext(), R.string.enable_notifications,Toast.LENGTH_SHORT).show();
            notificationsEnabled = true;
            btnNotification.setText(R.string.btn_notifications_stop);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
            builder.setContentTitle(getString(R.string.app_name));
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentText(getString(R.string.test_notification));
            NotificationManagerCompat.from(getContext()).notify(0, builder.build());

        }else{
            Toast.makeText(getContext(), R.string.disable_notifications,Toast.LENGTH_SHORT).show();
            notificationsEnabled = false;
            btnNotification.setText(R.string.btn_notifications);
        }
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
        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
}
