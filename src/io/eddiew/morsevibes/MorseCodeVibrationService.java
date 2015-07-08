package io.eddiew.morsevibes;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Vibrator;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by eddiew on 7/1/15.
 */
public class MorseCodeVibrationService extends NotificationListenerService {
    public static final int DOT_DURATION = 100;

    @Override
    public void onListenerConnected() {
        Log.d("service", "Notification listener connected");
        MainActivity.VibrationOverrideServiceRunning = true;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Don't vibrate if the notification doesn't vibrate or if the phone is in silent mode
        if (!vibrates(sbn)) return;
        AudioManager audioMan = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioMan.getRingerMode() == AudioManager.RINGER_MODE_SILENT) return;

        Log.d("service", "vibrating notification received");
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Cancel existing vibrate pattern
        vibrator.cancel();

        // Get the new vibrate pattern
        char notificationLetter = getNotificationLetter(sbn);
        long[] vibrationPattern = getVibrationPattern(notificationLetter, DOT_DURATION);

        // Vibrate according to the new pattern
        vibrator.vibrate(vibrationPattern, -1);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // empty, but must exist bc method is abstract in NotificationListenerService until API 21
    }

    private boolean vibrates(StatusBarNotification sbn) {
        // Get whether sbn uses the default vibrate
        Notification notification = sbn.getNotification();
        boolean defaultVibrate = (notification.defaults & Notification.DEFAULT_VIBRATE) != 0;

        // Get whether sbn uses a custom vibration pattern
        long[] vibrationPattern = sbn.getNotification().vibrate;
        boolean customVibrate = vibrationPattern != null && vibrationPattern.length > 1;

        // Gmail hack because it doesn't use notification vibrations
        boolean isGMail = sbn.getPackageName().equals("com.google.android.gm");

        return defaultVibrate || customVibrate || isGMail;
    }

    /**
     * Gets the vibration pattern for a given notification
     * based on the name of the application that issued it
     * @param letter the letter whose vibration pattern is needed
     * @return the vibration pattern for sbn
     */
    private long[] getVibrationPattern(char letter, long dotLen) {
        // Get morse code for the first letter of the notifying application's name
        int[] morseCode = getMorseCode(letter);

        // Convert morse code into vibrations
        long[] vibrationPattern = new long[morseCode.length*2];
        for(int i = 0; i < morseCode.length; i++) {
            vibrationPattern[i*2] = dotLen;
            vibrationPattern[i*2+1] = morseCode[i] * dotLen;
        }
        vibrationPattern[0] = 0;
        return vibrationPattern;
    }

    /**
     * Gets the name of the application that issued the notification sbn
     * @param sbn The notification whose application name we want to find
     * @return the name of the application that issued sbn
     */
    private char getNotificationLetter(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            CharSequence appName = packageManager.getApplicationLabel(applicationInfo);
            Log.d("service", appName.toString());
            return appName.charAt(0);
        }
        // This should never happen unless the os itself fucks up
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 'm'; // m -> dash dash
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
