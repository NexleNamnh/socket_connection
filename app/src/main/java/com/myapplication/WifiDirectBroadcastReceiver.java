package com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Unkown on 2/20/17.
 */

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = WifiDirectBroadcastReceiver.class.getSimpleName();
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    public static WifiP2pInfo wifiP2pInfo;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.wifiP2pManager = manager;
        this.channel = channel;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "Wifi Direct is enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Wifi Direct is not enabled", Toast.LENGTH_SHORT).show();
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //This broadcast is sent when status of in range peers changes. Attempt to get current list of peers.
            wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {

                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    CommonUtils.hideLoading();
                    Intent peerIntent = new Intent(Constant.ACTION_PEERS_AVAILABLE);
                    peerIntent.putExtra(Constant.KEY_PEERS_AVAILABLE, peers);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(peerIntent);
                }
            });

            //update UI with list of peers

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
//            WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
//            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if (networkState.isConnected()) {
                wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        Log.d(TAG, "onConnectionInfoAvailable: " + info.groupOwnerAddress.getHostAddress());
                        wifiP2pInfo = info;
                        Intent connectionInfoIntent = new Intent(Constant.ACTION_CONNECTION_INFO);
                        connectionInfoIntent.putExtra(Constant.KEY_CONNECTION_INFO, info);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(connectionInfoIntent);

                        // start the server for listening
                        Intent serverIntent = new Intent(context, ServerService.class);
                        context.startService(serverIntent);

                        // start the client for sending
                        Intent clientIntent = new Intent(context, ClientService.class);
                        clientIntent.putExtra(Constant.KEY_CLIENT_ADDRESS, info.groupOwnerAddress.getHostAddress());
                        clientIntent.putExtra(Constant.KEY_MESSAGE_TO_SEND, "");
                        context.startService(clientIntent);
                    }
                });
            } else {
                //set variables to disable file transfer and reset client back to original state
                wifiP2pManager.cancelConnect(channel, null);
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constant.ACTION_CONNECTION_DISCONNECTED));
            }
            //activity.setClientStatus(networkState.isConnected());
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
