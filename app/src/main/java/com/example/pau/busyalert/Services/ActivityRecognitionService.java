package com.example.pau.busyalert.Services;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.pau.busyalert.Interfaces.HerokuEndpointInterface;
import com.example.pau.busyalert.JavaClasses.ApiUtils;
import com.example.pau.busyalert.JavaClasses.HerokuLog;
import com.example.pau.busyalert.R;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ActivityRecognitionService extends IntentService {
    private boolean bike, running;
    private HerokuEndpointInterface apiService = ApiUtils.getAPIService();

    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ActivityRecognitionResult.hasResult(intent)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            bike = sharedPreferences.getBoolean("bike", false);
            running = sharedPreferences.getBoolean("running", false);
            String status = sharedPreferences.getString("status", "Available");

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            boolean isBusy = handleDetectedActivity(result.getMostProbableActivity());

            //If the user is already busied, the notifications is not sent.
            if(status.equals("Available") && isBusy){
                setStatus("Busy");
            }else{
                setStatus("Available");
            }
        }
    }

    private boolean handleDetectedActivity(DetectedActivity detectedActivity){
        int confidence = detectedActivity.getConfidence();

        if(confidence >= 75){
            if(detectedActivity.getType() == DetectedActivity.IN_VEHICLE){
                return true;
            }else if(bike && detectedActivity.getType() == DetectedActivity.ON_BICYCLE){
                return true;
            }else if(running && detectedActivity.getType() == DetectedActivity.RUNNING) {
                return true;
            }
        }
        return false;
    }

    private void setStatus(String status){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("status", status);
        editor.apply();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).child("status").setValue(status);

        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

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

}
