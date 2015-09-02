package io.eddiew.morsevibes;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by eddiew on 7/7/15.
 */
public class SettingsActivity extends PreferenceActivity {
    private String dotLengthKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        dotLengthKey = getString(R.string.dotLength);
//
//        // Listen for dot length changes
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        prefs.registerOnSharedPreferenceChangeListener(dotLengthListener);

        // Initialize preferences activity
        MorsePreferenceFragment morsePrefs = new MorsePreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, morsePrefs).commit();
        morsePrefs.init(this);
    }

    private void createNotification(CharSequence title, CharSequence text) {
        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);
        mBuilder.setAutoCancel(true);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        Notification notification = mBuilder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    // Returns whether MorseCodeVibrationService can read the user's notifications
    private boolean hasNotificationAccess() {
        String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(MorseCodeVibrationService.class.getName());
    }

    public static class MorsePreferenceFragment extends PreferenceFragment
    {
        SettingsActivity settingsActivity;
        CheckBoxPreference enabled;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public void onResume() {
            super.onResume();
            if (enabled != null && settingsActivity != null) enabled.setChecked(settingsActivity.hasNotificationAccess());
        }

        public void init(final SettingsActivity settingsActivity) {
            this.settingsActivity = settingsActivity;
            settingsActivity.getFragmentManager().executePendingTransactions();

            // Setup dotLength preference to change its hint to whatever value is entered
            EditTextPreference dotLength = (EditTextPreference) findPreference(getString(R.string.dotLength));
            dotLength.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue){
                    preference.setSummary((CharSequence) newValue);
                    return true;
                }
            });
            // Set initial value
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(settingsActivity);
            dotLength.setSummary(sharedPrefs.getString(getString(R.string.dotLength), "120"));

            // Setup enabled preference to open the notification listener settings activity
            enabled = (CheckBoxPreference) findPreference(getString(R.string.enabled));
            enabled.setChecked(settingsActivity.hasNotificationAccess());
            enabled.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // open Notification Access Panel
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    return true;
                }
            });

            // Setup test preference to create a dummy notification when clicked
            Preference testButton = findPreference(getString(R.string.test));
            testButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    settingsActivity.createNotification(getString(R.string.app_name), getString(R.string.test_message));
                    return true;
                }
            });
        }
    }

//    private SharedPreferences.OnSharedPreferenceChangeListener dotLengthListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
//        @Override
//        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//            if (!key.equals(dotLengthKey))
//                return;
//            Log.d("Dot length listener", "dot length changed");
//
//            // Get new vibration patterns
//            String dotLengthString = sharedPreferences.getString(key, "120");
//            long dotLength = Long.parseLong(dotLengthString);
//            long[][] vibrationPatterns = getVibrationPatterns(dotLength);
//        }
//    };

    private long[][] getVibrationPatterns(long dotLength) {
        long[][] patterns = new long[26][];
        for (int c = 0; c < 26; c++) {
            int morseLength = MorseCodes[c].length;
            patterns[c] = new long[morseLength * 2];
            for (int i = 0; i < morseLength; i++) {
                patterns[c][i * 2] = dotLength;
                patterns[c][i * 2 + 1] = MorseCodes[c][i] * dotLength;
            }
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
