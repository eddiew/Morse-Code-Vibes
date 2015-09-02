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
  private long[][] vibrationPatterns = new long[][]{{}};

  @Override
  public void onListenerConnected() {
    Log.d("service", "Notification listener connected");
    Log.v("dsfdsa", "wtf");
    Log.d("dsfdsa", "wtf");
    Log.i("dsfdsa", "wtf");
    Log.w("dsfdsa", "wtf");
    Log.e("dsfdsa", "wtf");
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    // Cancel existing vibrate. Done up front to reduce latency
    vibrator.cancel();

    Log.v("shit", "wtf");
    Log.d("shit", "wtf");
    Log.i("shit", "wtf");
    Log.w("shit", "wtf");
    Log.e("shit", "wtf");

//    // Don't vibrate if the notification doesn't vibrate or if the phone is in silent mode
//    AudioManager audioMan = (AudioManager) getSystemService(AUDIO_SERVICE);
//    if (!vibrates(sbn) || audioMan.getRingerMode() == AudioManager.RINGER_MODE_SILENT) return;
//
//    Log.d("service", "vibrating notification received");
//
//    // Vibrate according to the new pattern
//    long[] vibrationPattern = getVibrationPattern(sbn);
//    vibrator.vibrate(vibrationPattern, -1);
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

    return defaultVibrate || customVibrate;
  }

  /**
   * Gets the vibration pattern for a given notification
   * based on the name of the application that issued it
   * @param sbn the notification whose vibration pattern we want
   * @return the vibration pattern for sbn
   */
  private long[] getVibrationPattern(StatusBarNotification sbn) {
    // Get morse code for the first letter of the notifying application's name
    char c = getAppLetter(sbn);
    return vibrationPatterns[c - 'a'];
  }

  /**
   * Gets the name of the application that issued the notification sbn
   * @param sbn The notification whose application name we want to find
   * @return the lowercase first letter of the application that issued sbn
   */
  private char getAppLetter(StatusBarNotification sbn) {
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

  @Override
  public void onDestroy() {
    Log.d("Morse vibes service", "destroyed");
    super.onDestroy();
  }
}
