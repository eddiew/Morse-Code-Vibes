package io.eddiew.morsevibes;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.provider.Settings;

/**
 * Created by eddiew on 7/7/15.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
}
