package mapnotes.mapnotes;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mapnotes.mapnotes.activities.GoogleSignIn;

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
        email = sp.getString("email", "");
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
                                long start_time = Long.valueOf(data.get("start_time"));
                                long end_time = Long.valueOf(data.get("end_time"));
                                String user = data.get("user");
                                float[] results = new float[1];
                                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                        latitude, longitude, results);

                                long currentTime = Calendar.getInstance().getTimeInMillis();
                                if (results[0] < 1000 && email != null && !email.equals(user)
                                        && currentTime >= start_time && currentTime <= end_time) {
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

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String ringtonePreference = sp.getString("notifications_new_note_ringtone", "DEFAULT_RINGTONE_URI");
        // The key of preference was "@string/ringtonePref" which is useless since you're hardcoding the string here anyway.
        Uri ringtoneUri = Uri.parse(ringtonePreference);
        boolean vibrate = sp.getBoolean("notifications_new_message_vibrate", true);
        long[] vibrateTime = {1000, 1000, 1000, 1000, 1000};
        if (!vibrate) {
            vibrateTime = null;
        }

        String channelId = "234";
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

        notificationManager.notify(0123, notificationBuilder.build());
    }
}
