package com.example.customvibes;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by eddiew on 7/1/15.
 */
public class VibrationOverrideService extends NotificationListenerService {
    public static final int DOT_DURATION = 100;

    @Override
    public void onListenerConnected() {
        Log.d("service", "Notification listener connected");
        MainActivity.VibrationOverrideServiceRunning = true;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!vibrates(sbn)) return;

        Log.d("service", "vibrating notification received");
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Cancel existing vibrate pattern
        vibrator.cancel();

        // Vibrate according to the new pattern
        long[] vibrationPattern = getVibrationPattern(sbn);
        vibrator.vibrate(vibrationPattern, -1, sbn.getNotification().audioAttributes);
    }

    private boolean vibrates(StatusBarNotification sbn) {
        // Get whether sbn uses the default vibrate
        Notification notification = sbn.getNotification();
        boolean defaultVibrate = (notification.defaults & Notification.DEFAULT_VIBRATE) != 0;

        // Get whether sbn uses a custom vibration pattern
        long[] vibrationPattern = sbn.getNotification().vibrate;
        boolean customVibrate = vibrationPattern != null && vibrationPattern.length > 1;

        return defaultVibrate || customVibrate;
    }

    /**
     * Gets the vibration pattern for a given notification
     * based on the name of the application that issued it
     * @param sbn the notification whose vibration pattern we need
     * @return the vibration pattern for sbn
     */
    private long[] getVibrationPattern(StatusBarNotification sbn) {
        // Get morse code for the first letter of the notifying application's name
        CharSequence appName = getNotifyingAppName(sbn);
        Log.d("service", appName.toString());
        int[] morseCode = getMorseCode(appName.charAt(0));

        // Convert morse code into vibrations
        long[] vibrationPattern = new long[morseCode.length*2];
        for(int i = 0; i < morseCode.length; i++) {
            vibrationPattern[i*2] = DOT_DURATION;
            vibrationPattern[i*2+1] = morseCode[i] * DOT_DURATION;
        }
        vibrationPattern[0] = 0;
        return vibrationPattern;
    }

    /**
     * Gets the name of the application that issued the notification sbn
     * @param sbn The notification whose application name we want to find
     * @return the name of the application that issued sbn
     */
    private CharSequence getNotifyingAppName(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(applicationInfo);
        }
        // This should never happen unless the os itself fucks up
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Application Not Found";
        }
    }

    private int[] getMorseCode(char c) {
        if (c <= 'Z') c += 'a' - 'A';
        switch (c) {
            case 'a': return new int[]{1,3};
            case 'b': return new int[]{3,1,1,1};
            case 'c': return new int[]{3,1,3,1};
            case 'd': return new int[]{3,1,1};
            case 'e': return new int[]{1};
            case 'f': return new int[]{1,1,3,1};
            case 'g': return new int[]{3,3,1};
            case 'h': return new int[]{1,1,1,1};
            case 'i': return new int[]{1,1};
            case 'j': return new int[]{1,3,3,3};
            case 'k': return new int[]{3,1,3};
            case 'l': return new int[]{1,3,1,1};
            case 'm': return new int[]{3,3};
            case 'n': return new int[]{3,1};
            case 'o': return new int[]{3,3,3};
            case 'p': return new int[]{1,3,3,1};
            case 'q': return new int[]{3,3,1,3};
            case 'r': return new int[]{1,3,1};
            case 's': return new int[]{1,1,1};
            case 't': return new int[]{3};
            case 'u': return new int[]{1,1,3};
            case 'v': return new int[]{1,1,1,3};
            case 'w': return new int[]{1,3,3};
            case 'x': return new int[]{3,1,1,3};
            case 'y': return new int[]{3,1,3,3};
            case 'z': return new int[]{3,3,1,1};
        }
        return new int[0];
    }

    @Override
    public void onDestroy() {
        MainActivity.VibrationOverrideServiceRunning = false;
        Log.d("service", "destroyed");
        super.onDestroy();
    }
}
