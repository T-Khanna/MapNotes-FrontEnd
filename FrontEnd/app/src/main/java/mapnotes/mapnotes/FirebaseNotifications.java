package mapnotes.mapnotes;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mapnotes.mapnotes.activities.GoogleSignIn;

public class FirebaseNotifications extends FirebaseMessagingService {

    @Override
    public void onDeletedMessages() {

    }

    @Override
    public void onMessageReceived(final RemoteMessage message) {
        // Check if message contains a data payload.
        if (message.getData().size() > 0) {
            // Handle message within 10 seconds
            Executor executor = Executors.newSingleThreadExecutor();

            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(executor, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                Map<String, String> data = message.getData();
                                double latitude = Double.valueOf(data.get("latitude"));
                                double longitude = Double.valueOf(data.get("longitude"));
                                float[] results = new float[1];
                                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                        latitude, longitude, results);
                                if (results[0] < 1000) {
                                    sendNotification("New event less than 1 km away!");
                                }
                            }
                        });
            }
        }
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, GoogleSignIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "234";
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_map)
                        .setContentTitle("New Event")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0123, notificationBuilder.build());
    }
}
