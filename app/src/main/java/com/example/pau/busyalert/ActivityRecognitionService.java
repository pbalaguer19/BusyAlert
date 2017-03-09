package com.example.pau.busyalert;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ActivityRecognitionService extends IntentService {
    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivity(result.getProbableActivities());
        }
    }

    private void handleDetectedActivity(List<DetectedActivity> probableActivities){
        for(DetectedActivity detectedActivity: probableActivities){
            if(detectedActivity.getType() == DetectedActivity.IN_VEHICLE &&
                    detectedActivity.getConfidence() > 70){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentText(getString(R.string.driving));
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setContentTitle(getString(R.string.app_name));
                NotificationManagerCompat.from(this).notify(0, builder.build());
            }else{
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentText(getString(R.string.driving));
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setContentTitle(getString(R.string.app_name));
                NotificationManagerCompat.from(this).notify(0, builder.build());
            }
        }
    }
}
