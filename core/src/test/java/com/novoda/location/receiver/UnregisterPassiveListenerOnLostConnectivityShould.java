package com.novoda.location.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class UnregisterPassiveListenerOnLostConnectivityShould {

    static final NetworkInfo NO_ACTIVE_NETWORK_INFO = null;

    final Context context = mock(Context.class);
    final PackageManager packageManager = mock(PackageManager.class);
    final NetworkInfo activeNetworkInfo = mock(NetworkInfo.class);

    @Before
    public void setUp() throws Exception {
        when(context.getPackageManager()).thenReturn(packageManager);
        addNetworkInfoToConnectivityManager(context, activeNetworkInfo);
    }

    private void addNetworkInfoToConnectivityManager(Context context, NetworkInfo activeNetworkInfo) {
        ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(activeNetworkInfo);
        when(context.getSystemService(eq(Context.CONNECTIVITY_SERVICE))).thenReturn(connectivityManager);
    }

    private void verifyNoReceiverIsEnabled() {
        verify(packageManager, never()).setComponentEnabledSetting(any(ComponentName.class), anyInt(), anyInt());
    }

    private void verifyReceiverHasBeenEnabled(Class<? extends BroadcastReceiver> receiver) {
        ComponentName componentName = new ComponentName(context, receiver);
        verify(packageManager).setComponentEnabledSetting(eq(componentName), eq(COMPONENT_ENABLED_STATE_DEFAULT), eq(DONT_KILL_APP));
    }

    private void onConnectivityChanged() {
        UnregisterPassiveListenerOnLostConnectivity unregisterPassiveListenerOnLostConnectivity = new UnregisterPassiveListenerOnLostConnectivity();
        unregisterPassiveListenerOnLostConnectivity.onReceive(context, new Intent());
    }

    @Test
    public void NOT_enable_receivers_if_there_is_no_active_network() throws Exception {
        addNetworkInfoToConnectivityManager(context, NO_ACTIVE_NETWORK_INFO);

        onConnectivityChanged();

        verifyNoReceiverIsEnabled();
    }

    @Test
    public void NOT_enable_receivers_if_the_active_network_is_not_connected_or_connecting() throws Exception {
        when(activeNetworkInfo.isConnectedOrConnecting()).thenReturn(false);

        onConnectivityChanged();

        verifyNoReceiverIsEnabled();
    }

    @Test
    public void enable_itself_if_the_active_network_is_connected_or_connecting() throws Exception {
        when(activeNetworkInfo.isConnectedOrConnecting()).thenReturn(true);

        onConnectivityChanged();

        verifyReceiverHasBeenEnabled(UnregisterPassiveListenerOnLostConnectivity.class);
    }

    @Test
    public void enable_the_location_changed_receiver_if_the_active_network_is_connected_or_connecting() throws Exception {
        when(activeNetworkInfo.isConnectedOrConnecting()).thenReturn(true);

        onConnectivityChanged();

        verifyReceiverHasBeenEnabled(LocationChanged.class);
    }

    @Test
    public void enable_the_passive_location_changed_receiver_if_the_active_network_is_connected_or_connecting() throws Exception {
        when(activeNetworkInfo.isConnectedOrConnecting()).thenReturn(true);

        onConnectivityChanged();

        verifyReceiverHasBeenEnabled(PassiveLocationChanged.class);
    }


}
