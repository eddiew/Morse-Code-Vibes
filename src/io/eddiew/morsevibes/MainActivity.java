package io.eddiew.morsevibes;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import com.example.morsevibes.R;

public class MainActivity extends Activity {
    static boolean VibrationOverrideServiceRunning = false;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Prompt user for notification access if we don't already have it
        if (!hasNotificationAccess())
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    private void createNotification() {
        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("Dummy title");
        mBuilder.setContentText("Dummy text");
        mBuilder.setAutoCancel(true);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        Notification notification = mBuilder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    public void createNotification(View v) {
        createNotification();
    }

    // Returns whether MorseCodeVibrationService can read the user's notifications
    private boolean hasNotificationAccess() {
        String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return enabledNotificationListeners.contains(MorseCodeVibrationService.class.getName());
    }
}
