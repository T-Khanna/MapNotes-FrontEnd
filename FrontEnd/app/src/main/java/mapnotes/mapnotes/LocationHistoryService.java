package mapnotes.mapnotes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import mapnotes.mapnotes.data_classes.DateAndTime;
import mapnotes.mapnotes.data_classes.EventCache;
import mapnotes.mapnotes.data_classes.Function;
import mapnotes.mapnotes.data_classes.Note;

public class LocationHistoryService extends Service
{
    private static final String TAG = "MAPNOTESBACKGROUND";
    private LocationManager mLocationManager = null;
    private static final long LOCATION_INTERVAL = 900000; //15 minutes (milliseconds)
    private static final float LOCATION_DISTANCE = 100f; //100 meters
    private EventCache eventCache;

    private class LocationListener implements android.location.LocationListener
    {
        private Location mLastLocation;
        private Server server;

        public LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged");

            float[] results = new float[3];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    mLastLocation.getLatitude(), mLastLocation.getLongitude(), results);

            mLastLocation.set(location);

            if (results[0] >= LOCATION_DISTANCE) {
                GoogleSignInAccount result = GoogleSignIn.getLastSignedInAccount(LocationHistoryService.this);
                if (result != null && result.getIdToken() != null) {
                    server = new Server(LocationHistoryService.this, result.getIdToken());
                    getNotes(new DateAndTime(Calendar.getInstance()));
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        private void getNotes(DateAndTime date) {
            try {
                String url = "api/notes/time/\"" + date.toString() + "\"";
                server.getJSONRequest(url, null, new Function<JSONObject>() {
                    @Override
                    public void run(JSONObject input) {
                        try {
                            if (input.has("Notes")) {
                                JSONArray array = input.getJSONArray("Notes");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonNote = array.getJSONObject(i);
                                    Note note = new Note(jsonNote);
                                    float[] results = new float[3];

                                    Location.distanceBetween(note.getLocation().latitude, note.getLocation().longitude,
                                            mLastLocation.getLatitude(), mLastLocation.getLongitude(), results);

                                    //If within 50 meters
                                    if (results[0] >= 0 && results[0] < 50) {
                                        if (!eventCache.contains(note.getTitle())) {
                                            sendNotification("Are you at " + note.getTitle() + "?", note.getId());
                                        }
                                        eventCache.add(note.getTitle());
                                        try {
                                            eventCache.save();
                                        } catch (IOException e){
                                            Log.e(TAG, "Unable to save eventcache, " + e.getMessage());
                                            e.printStackTrace();

                                        }
                                        break;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendNotification(String messageBody, int eventID) {
            Intent intent = new Intent(LocationHistoryService.this, HistoryBroadcastReceiver.class);
            PendingIntent noIntent = PendingIntent.getBroadcast(LocationHistoryService.this,
                    1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Intent intent2 = new Intent(LocationHistoryService.this, HistoryBroadcastReceiver.class);
            intent2.putExtra("eventID", eventID);
            PendingIntent yesIntent = PendingIntent.getBroadcast(LocationHistoryService.this,
                    0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);

            String channelId = "Location_History_ID";
            initChannels(LocationHistoryService.this, channelId, "Location History Service");
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(LocationHistoryService.this, channelId)
                            .setSmallIcon(R.drawable.ic_stat_map)
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .addAction(0, "NO", noIntent)
                            .addAction(0, "YES", yesIntent);


            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        }

        private void initChannels(Context context, String channelID, String description) {
            if (Build.VERSION.SDK_INT < 26) {
                return;
            }
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID,
                    "LocationHistoryService",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    LocationListener mLocationListeners = new LocationListener(LocationManager.NETWORK_PROVIDER);

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        eventCache = new EventCache(this);
        eventCache.clean();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListeners);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}