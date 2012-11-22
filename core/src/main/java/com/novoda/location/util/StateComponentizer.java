package com.novoda.location.util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import com.novoda.location.receiver.PassiveLocationChanged;

public class StateComponentizer {

    public static void changeStateToComponent(Context c, int state) {
        PackageManager pm = c.getPackageManager();
        ComponentName cr = new ComponentName(c, PassiveLocationChanged.class);
        pm.setComponentEnabledSetting(cr, state, PackageManager.DONT_KILL_APP);
    }

    public static void changeStateToComponent(Context c, Class<? extends BroadcastReceiver> clazz) {
        PackageManager pm = c.getPackageManager();
        ComponentName cr = new ComponentName(c, clazz);
        pm.setComponentEnabledSetting(cr, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP);

    }

}
