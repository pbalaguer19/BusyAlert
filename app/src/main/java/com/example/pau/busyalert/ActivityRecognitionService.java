package com.example.pau.busyalert;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


public class ActivityRecognitionService extends IntentService {
    private boolean bike, running;

    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ActivityRecognitionResult.hasResult(intent)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            bike = sharedPreferences.getBoolean("bike", false);
            running = sharedPreferences.getBoolean("running", false);
            String status = sharedPreferences.getString("status", "Available");

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            String response = handleDetectedActivity(result.getMostProbableActivity());

            //If the user is already busied, the notifications is not sent.
            if(status.equals("Available") && response != null){
                sendNotification(response);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("status", "Busy");
                editor.apply();
            }
        }
    }

    private void sendNotification(String text){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText(text);
        NotificationManagerCompat.from(this).notify(0, builder.build());
    }

    private String handleDetectedActivity(DetectedActivity detectedActivity){
        int confidence = detectedActivity.getConfidence();

        if(detectedActivity.getType() == DetectedActivity.IN_VEHICLE &&
                confidence > 75){
            return getString(R.string.driving);
        }else if(bike && detectedActivity.getType() == DetectedActivity.ON_BICYCLE &&
                confidence > 75){
            return getString(R.string.driving_bike);
        }else if(running && detectedActivity.getType() == DetectedActivity.RUNNING &&
                confidence > 75) {
            return getString(R.string.running);
        }
        return null;
    }

}
