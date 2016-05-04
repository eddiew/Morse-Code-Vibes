package io.eddiew.morsecodevibes;

import android.app.Notification;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;

/**
* Created by eddiew on 7/1/15.
*/
public class MorseCodeVibrationService extends NotificationListenerService implements SharedPreferences.OnSharedPreferenceChangeListener {
  private long[][] vibrationPatterns = new long[26][];
  private Vibrator vibrator;
  private AudioManager audioMan;
	PackageManager packMan;
	private HashMap<String, long[]> appPatternCache = new HashMap<>(); // It says cache, but really we remember everything. Nobody has 1000s of apps installed, right?

  @Override
  public void onListenerConnected() {
    Log.d("Morse vibes service", "connected");

    vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    audioMan = (AudioManager) getSystemService(AUDIO_SERVICE);
	  packMan = getPackageManager();

    // Listen for dot length changes
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    prefs.registerOnSharedPreferenceChangeListener(this);
    onSharedPreferenceChanged(prefs, getString(R.string.dotLength));
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    // Cancel existing vibrate. Done up front to reduce latency
    vibrator.cancel();

	  Notification notification = sbn.getNotification();

    // Don't vibrate if the notification doesn't vibrate or if the phone is in silent mode
    if (!vibrates(notification) || audioMan.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
      return;

//    Log.d("service", "vibrating notification received");

	  String packageName = sbn.getPackageName();

	  // Check cache for app's vibration pattern
	  long[] vibrationPattern = appPatternCache.get(packageName);

		// Compute vibration pattern and store in cache if not seen before
	  if (vibrationPattern == null) {
		  vibrationPattern = getVibrationPattern(packageName);
		  appPatternCache.put(packageName, vibrationPattern);
	  }

	  // Get notification's audio attributes
	  AudioAttributes audioAttributes = notification.audioAttributes;

    // Vibrate according to the new pattern
    vibrator.vibrate(vibrationPattern, -1, audioAttributes);
  }

  private boolean vibrates(Notification notification) {
    // Whether @notification uses the default vibrate
    boolean defaultVibrate = (notification.defaults & Notification.DEFAULT_VIBRATE) != 0;
	  if (defaultVibrate)
		  return true;

	  // Whether @notification uses a custom vibration pattern
	  long[] vibrationPattern = notification.vibrate;
	  return vibrationPattern != null && vibrationPattern.length > 0;
  }

  /**
   * Gets the vibration pattern for a given notification
   * based on the name of the application that issued it
   * @param packageName The package name that sent the notification
   * @return the vibration pattern for the app that owns @packageName
   */
  private long[] getVibrationPattern(String packageName) {
    try {
	  char c = getAppsFirstLetter(packageName);
	  return vibrationPatterns[c - 'a'];
  }
	// This should never happen unless the os itself fucks up
	catch (PackageManager.NameNotFoundException e) {
		Log.e("Morse vibes service", "Error: " + e.toString());
		e.printStackTrace();
		return vibrationPatterns['m']; // m -> dash dash
	}
  }

	/**
	 * Gets the first letter of the application with package name @packageName
	 * @param packageName The package name for the app we want to look up
	 * @return the lowercase first letter of the application that issued sbn
	 * @throws PackageManager.NameNotFoundException if @packageName does not correspond to an application
	 */
	private char getAppsFirstLetter(String packageName) throws PackageManager.NameNotFoundException {
		ApplicationInfo applicationInfo = packMan.getApplicationInfo(packageName, 0);
		CharSequence appName = packMan.getApplicationLabel(applicationInfo);

		Log.d("Morse vibes service", "Received notification from new application: " + appName.toString());

		char c = appName.charAt(0);

		// Convert c to lowercase
		if (c <= 'Z')
			c += 'a' - 'A';

		return c;
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

	  // create patterns
    for (int c = 0; c < 26; c++) {
	    // create empty pattern
      int morseLength = MorseCodes[c].length;
      patterns[c] = new long[morseLength * 2];

	    // compute pattern
      for (int i = 0; i < morseLength; i++) {
        patterns[c][i * 2] = dotLength; // length of pause between vibrations
        patterns[c][i * 2 + 1] = MorseCodes[c][i] * dotLength; // length of vibration
      }

	    // the first term (delay) in each pattern should be 0
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
