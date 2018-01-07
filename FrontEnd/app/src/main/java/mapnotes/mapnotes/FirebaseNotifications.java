package mapnotes.mapnotes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mapnotes.mapnotes.activities.GoogleSignInActivity;
import mapnotes.mapnotes.activities.SplashScreenActivity;

public class FirebaseNotifications extends FirebaseMessagingService {

    String email = null;

    public FirebaseNotifications() {

    }

    @Override
    public void onDeletedMessages() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        email = sp.getString("user_id", "");
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
                                String user = data.get("user_id");
                                float[] results = new float[1];
                                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                        latitude, longitude, results);

                                if (results[0] < 1000 && email != null && !email.equals(user)) {
                                    sendNotification("New event less than 1 km away!");
                                }
                            }
                        });
            }
        }
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String ringtonePreference = sp.getString("notifications_new_note_ringtone", "DEFAULT_RINGTONE_URI");
        // The key of preference was "@string/ringtonePref" which is useless since you're hardcoding the string here anyway.
        Uri ringtoneUri = Uri.parse(ringtonePreference);
        boolean vibrate = sp.getBoolean("notifications_new_message_vibrate", true);
        long[] vibrateTime = {1000, 1000, 1000, 1000, 1000};
        if (!vibrate) {
            vibrateTime = null;
        }

        String channelId = "Firebase_notifications";
        initChannels(channelId, "Firebase Service");
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_map)
                        .setContentTitle("New Event")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setVibrate(vibrateTime)
                        .setSound(ringtoneUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, notificationBuilder.build());
    }

    private void initChannels(String channelID, String description) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelID,
                "FirebaseService",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        notificationManager.createNotificationChannel(channel);
    }
}
