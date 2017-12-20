package mapnotes.mapnotes;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Calendar;

import mapnotes.mapnotes.data_classes.DateAndTime;

/**
 * Created by Thomas on 29/11/2017.
 */

public class HistoryBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("BR", "Dismissing notification");
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);

        if (intent.hasExtra("eventID")) {
            int id = intent.getIntExtra("eventID", 0);
            //Send update to server
            GoogleSignInAccount result = GoogleSignIn.getLastSignedInAccount(context);
            if (result != null && result.getIdToken() != null) {
                Server server = new Server(context, result.getIdToken());
                Log.e("BR", "Send update to server");
            }
        }
    }
}
