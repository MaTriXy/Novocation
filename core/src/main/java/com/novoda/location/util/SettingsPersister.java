package com.novoda.location.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.novoda.location.LocatorSettings;

public class SettingsPersister {

    private static final String SHARED_PREFERENCE_FILE = "novocation_prefs";
    private static final String SP_KEY_RUN_ONCE = "sp_key_run_once";
    private static final String SP_KEY_PASSIVE_LOCATION_CHANGES = "sp_key_follow_location_changes";
    private static final String SP_KEY_PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF = "sp_passive_location_updates_distance_diff";
    private static final String SP_KEY_PASSIVE_LOCATION_UPDATES_INTERVAL = "sp_key_passive_location_updates_interval";

    public void persistSettingsToPreferences(Context context, LocatorSettings settings) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putBoolean(SP_KEY_PASSIVE_LOCATION_CHANGES, settings.shouldEnablePassiveUpdates());
        editor.putFloat(SP_KEY_PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF, settings.getPassiveUpdatesDistance());
        editor.putLong(SP_KEY_PASSIVE_LOCATION_UPDATES_INTERVAL, settings.getPassiveUpdatesInterval());
        editor.putBoolean(SP_KEY_RUN_ONCE, true);
        editor.commit();
    }

    private SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
    }

}
