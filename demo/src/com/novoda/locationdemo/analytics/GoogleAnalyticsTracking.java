package com.novoda.locationdemo.analytics;

import android.app.Activity;
import android.location.Location;

import com.google.analytics.tracking.android.EasyTracker;
import com.novoda.locationdemo.interfaces.AnalyticsTracking;

public class GoogleAnalyticsTracking implements AnalyticsTracking {

    public static interface PageView {
        String locationUpdateList = "/locationUpdateList";
        String noProviderAvailable = "/noProviderAvailable";
        String failure = "/failure";
        String unable_to_get_location = "/unable_to_get_location_";
        String on_pause_got_location = "/on_pause_got_location_";
        String not_accurate = "/not_accurate";
        String success = "/success";
    }

	private Activity activity;

	public GoogleAnalyticsTracking(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void setup() {
		EasyTracker.getInstance().setContext(activity);
	}

	@Override
	public void activityStart() {
		EasyTracker.getInstance().activityStart(activity);
	}

	@Override
	public void activityStop() {
		EasyTracker.getInstance().activityStop(activity);
	}

    @Override
    public void trackPageView(String pageView) {
        EasyTracker.getTracker().trackView(pageView);
    }

    public void trackLocationReceived(Location location, Location currentLocation, long time) {
        long deltaTimeToGetLocation = System.currentTimeMillis() - time;
        long deltaTimeToPreviousLocation = System.currentTimeMillis() - location.getTime();
        float accuracy = location.getAccuracy();

        String age = getAgeOfLocation(deltaTimeToPreviousLocation);
        String accuracyIndicator = getAccuracyIndicator(accuracy);
        String counter = isFirstOrAnother(currentLocation);
        String in = getTimeIndicator(deltaTimeToGetLocation);

        trackPageView("/" + counter + "_location_in_" + in + "_accuracy_" + accuracyIndicator + "_age_" + age);
    }

    private String getAgeOfLocation(long deltaTimeToPreviousLocation) {
        String age = "infinity";
        if (deltaTimeToPreviousLocation < 30000) {
            age = "<30s";
        } else if (deltaTimeToPreviousLocation < 60000) {
            age = "<1m";
        } else if (deltaTimeToPreviousLocation < 5 * 60 * 1000) {
            age = "<5m";
        } else if (deltaTimeToPreviousLocation < 30 * 60 * 1000) {
            age = "<30m";
        }
        return age;
    }

    private String isFirstOrAnother(Location currentLocation) {
        String counter = "another";
        if (currentLocation == null) {
            counter = "first";
        }
        return counter;
    }

    @Override
    public void trackLocationSuccessOrFailure(Location location, long time) {
        long deltaTimeToGetLocation = System.currentTimeMillis() - time;
        if (location == null) {
            trackPageView(PageView.unable_to_get_location + getTimeIndicator(deltaTimeToGetLocation));
            trackFailure(deltaTimeToGetLocation);
            return;
        }
        String accuracy = getAccuracyIndicator(location.getAccuracy());
        trackPageView(PageView.on_pause_got_location + getTimeIndicator(deltaTimeToGetLocation) + "_accuracy_" + accuracy);
        if (isFailure(location, deltaTimeToGetLocation)) {
            trackPageView(PageView.not_accurate);
        } else {
            trackPageView(PageView.success);
        }
    }

    private void trackFailure(long deltaTimeToGetLocation) {
        if (deltaTimeToGetLocation > 60000) {
            trackPageView(PageView.failure);
        }
    }

    private String getTimeIndicator(long deltaTimeToGetLocation) {
        String in = ">2m";
        if (deltaTimeToGetLocation <= 30000) {
            in = "<30s";
        } else if (deltaTimeToGetLocation <= 60000) {
            in = "<1m";
        } else if (deltaTimeToGetLocation <= 90000) {
            in = "<90s";
        } else if (deltaTimeToGetLocation <= 120000) {
            in = "<2m";
        }
        return in;
    }

    private String getAccuracyIndicator(float accuracy) {
        String accuracyIndicator = ">200";
        if (accuracy < 50) {
            accuracyIndicator = "<50";
        } else if (accuracy < 100) {
            accuracyIndicator = ">100";
        }
        return accuracyIndicator;
    }

    private boolean isFailure(Location location, long deltaTimeToGetLocation) {
        return deltaTimeToGetLocation < 2 * 60 * 1000 && location.getAccuracy() > 200;
    }
}
