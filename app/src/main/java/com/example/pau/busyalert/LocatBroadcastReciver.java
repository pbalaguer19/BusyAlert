package com.example.pau.busyalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;
import com.google.android.gms.location.LocationResult;

/**
 * Created by pau on 6/3/17.
 */
public class LocatBroadcastReciver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if(LocationResult.hasResult(intent)){
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();

            double speed = location.getSpeed();
            String text = "Location changed! Speed: " + speed;
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }
}
