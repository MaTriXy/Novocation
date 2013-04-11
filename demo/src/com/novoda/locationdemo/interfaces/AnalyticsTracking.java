package com.novoda.locationdemo.interfaces;

import android.location.Location;

public interface AnalyticsTracking {
	void setup();
	void activityStart();
	void activityStop();
    void trackPageView(String eventName);
    void trackLocationReceived(Location location, Location currentLocation, long time);
    void trackLocationSuccessOrFailure(Location location, long time);
}
