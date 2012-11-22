package com.novoda.location.util;

import com.novoda.location.provider.store.SettingsDao;
import com.novoda.location.provider.store.SharedPreferenceSettingsDao;

public class SettingsDaoUtil {

    public SettingsDao getSettingsDao() {
        return new SharedPreferenceSettingsDao();
    }

}
