package io.eddiew.morsevibes;

import android.app.Notification;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
* Created by eddiew on 7/1/15.
*/
public class MorseCodeVibrationService extends NotificationListenerService implements SharedPreferences.OnSharedPreferenceChangeListener {
  private long[][] vibrationPatterns = new long[26][];
  private Vibrator vibrator;
  private AudioManager audioMan;

  @Override
  public void onListenerConnected() {
    Log.d("service", "Notification listener connected");

    vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    audioMan = (AudioManager) getSystemService(AUDIO_SERVICE);

    // Listen for dot length changes
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    prefs.registerOnSharedPreferenceChangeListener(this);
    onSharedPreferenceChanged(prefs, getString(R.string.dotLength));
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    // Cancel existing vibrate. Done up front to reduce latency
    vibrator.cancel();

    // Don't vibrate if the notification doesn't vibrate or if the phone is in silent mode
    if (!vibrates(sbn) || audioMan.getRingerMode() == AudioManager.RINGER_MODE_SILENT) return;

    Log.d("service", "vibrating notification received");

    // Vibrate according to the new pattern
    long[] vibrationPattern = getVibrationPattern(sbn);
    vibrator.vibrate(vibrationPattern, -1);
  }

  @Override
  public void onNotificationRemoved(StatusBarNotification sbn) {
    // empty, but must exist bc method is abstract in NotificationListenerService until API 21
  }

  private boolean vibrates(StatusBarNotification sbn) {
    // Whether sbn uses the default vibrate
    Notification notification = sbn.getNotification();
    boolean defaultVibrate = (notification.defaults & Notification.DEFAULT_VIBRATE) != 0;

    // Whether sbn uses a custom vibration pattern
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
    char c = getAppsFirstLetter(sbn);
    return vibrationPatterns[c - 'a'];
  }

  /**
   * Gets the name of the application that issued the notification sbn
   * @param sbn The notification whose application name we want to find
   * @return the lowercase first letter of the application that issued sbn
   */
  private char getAppsFirstLetter(StatusBarNotification sbn) {
    String packageName = sbn.getPackageName();
    PackageManager packageManager = getPackageManager();
    try {
      ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
      CharSequence appName = packageManager.getApplicationLabel(applicationInfo);
      Log.d("service", appName.toString());
      char c = appName.charAt(0);
      if (c <= 'Z')
        c += 'a' - 'A';
      return c;
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

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    String dotLengthKey = getString(R.string.dotLength);
    if (!key.equals(dotLengthKey))
      return;
    Log.d("Dot length listener", "dot length changed");

    // Set new vibration patterns
    String dotLengthString = sharedPreferences.getString(dotLengthKey, "120");
    long dotLength = Long.parseLong(dotLengthString);
    vibrationPatterns = getVibrationPatterns(dotLength);
  }

  private static long[][] getVibrationPatterns(long dotLength) {
    long[][] patterns = new long[26][];
    for (int c = 0; c < 26; c++) {
      int morseLength = MorseCodes[c].length;
      patterns[c] = new long[morseLength * 2];
      for (int i = 0; i < morseLength; i++) {
        patterns[c][i * 2] = dotLength;
        patterns[c][i * 2 + 1] = MorseCodes[c][i] * dotLength;
      }
      patterns[c][0] = 0;
    }
    return patterns;
  }

  private static long[][] MorseCodes = new long[][] {
      {1,3},      // a
      {3,1,1,1},  // b
      {3,1,3,1},  // c
      {3,1,1},    // d
      {1},        // e
      {1,1,3,1},  // f
      {3,3,1},    // g
      {1,1,1,1},  // h
      {1,1},      // i
      {1,3,3,3},  // j
      {3,1,3},    // k
      {1,3,1,1},  // l
      {3,3},      // m
      {3,1},      // n
      {3,3,3},    // o
      {1,3,3,1},  // p
      {3,3,1,3},  // q
      {1,3,1},    // r
      {1,1,1},    // s
      {3},        // t
      {1,1,3},    // u
      {1,1,1,3},  // v
      {1,3,3},    // w
      {3,1,1,3},  // x
      {3,1,3,3},  // y
      {3,3,1,1}   // z
  };
}
