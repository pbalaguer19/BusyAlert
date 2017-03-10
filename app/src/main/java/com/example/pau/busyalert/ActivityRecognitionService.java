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

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivity(result.getMostProbableActivity());
        }
    }

    private void handleDetectedActivity(DetectedActivity detectedActivity){
        int confidence = detectedActivity.getConfidence();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setSmallIcon(R.mipmap.ic_launcher);

        if(detectedActivity.getType() == DetectedActivity.IN_VEHICLE &&
                confidence > 75){
            builder.setContentText(getString(R.string.driving));
        }else if(bike && detectedActivity.getType() == DetectedActivity.ON_BICYCLE &&
                confidence > 75){
            builder.setContentText(getString(R.string.driving_bike));
        }else if(running && detectedActivity.getType() == DetectedActivity.RUNNING &&
                confidence > 75) {
            builder.setContentText(getString(R.string.running));
        }else{
            builder.setContentText("Nothing");
        }
        NotificationManagerCompat.from(this).notify(0, builder.build());
    }

}
