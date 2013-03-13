package com.novoda.locationdemo.interfaces;

import android.location.Location;

public interface AnalyticsTracking {
	void setup();
	void activityStart();
	void activityStop();
    void trackEvent(String eventName, String action, String label, long value);
    void trackPageView(String eventName);
    void trackLocationSuccessOrFailure(Location location, long time);
}
