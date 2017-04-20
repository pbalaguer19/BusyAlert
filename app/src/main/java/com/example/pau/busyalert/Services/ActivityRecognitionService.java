package com.example.pau.busyalert.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.pau.busyalert.R;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ActivityRecognitionService extends IntentService {
    private boolean bike, running;

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
    }

}
