package com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifichannel;
    private BroadcastReceiver wifiClientReceiver;
    private IntentFilter wifiClientReceiverIntentFilter;
    private static MainActivity sActivity;

    public static MainActivity getActivity() {
        return sActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        sActivity = this;
        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifichannel = wifiManager.initialize(this, getMainLooper(), null);
        wifiClientReceiver = new WifiDirectBroadcastReceiver(wifiManager, wifichannel);

        wifiClientReceiverIntentFilter = new IntentFilter();
        ;
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(wifiClientReceiver, wifiClientReceiverIntentFilter);

        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, new DiscoverFragment()).commitAllowingStateLoss();

    }

    public void discoverPeers() {
        CommonUtils.showLoading("Discovering...");
        wifiManager.discoverPeers(wifichannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    public void connectToPeer(final WifiP2pDevice wifiPeer) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiPeer.deviceAddress;
        CommonUtils.showLoading("Connecting...");
        wifiManager.connect(wifichannel, config, new WifiP2pManager.ActionListener() {
            public void onSuccess() {

            }
            public void onFailure(int reason) {

            }
        });
    }

    public void disconnectP2p() {
        wifiManager.removeGroup(wifichannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(wifiClientReceiver);
        disconnectP2p();
        super.onDestroy();
    }


}
