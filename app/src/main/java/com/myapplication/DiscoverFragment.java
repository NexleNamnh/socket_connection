package com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Unkown on 2/20/17.
 */

public class DiscoverFragment extends BaseFragment {

    private ListView lvPeers;
    private PeersAdapter peersAdapter;
    private MainActivity activity;
    private P2pReceiver p2pReceiver;
    private TextView tvInfo;
    private Button btStartCommunication, btDisconnect;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_discover, container, false);
        }
        rootView.findViewById(R.id.bt_discover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.discoverPeers();
            }
        });
        tvInfo = (TextView) rootView.findViewById(R.id.tv_info);
        lvPeers = (ListView) rootView.findViewById(R.id.lv_peers);
        lvPeers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) parent.getAdapter().getItem(position);
                activity.connectToPeer(wifiP2pDevice);
            }
        });
        btStartCommunication = (Button) rootView.findViewById(R.id.bt_start_communication);
        btStartCommunication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, new CommunicationFragment()).addToBackStack(null).commitAllowingStateLoss();
            }
        });
        btDisconnect = (Button) rootView.findViewById(R.id.bt_disconnect);
        btDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.disconnectP2p();
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        p2pReceiver = new P2pReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_PEERS_AVAILABLE);
        intentFilter.addAction(Constant.ACTION_CONNECTION_INFO);
        intentFilter.addAction(Constant.ACTION_CONNECTION_DISCONNECTED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(p2pReceiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(p2pReceiver);
    }

    public void reset() {
        tvInfo.setVisibility(View.GONE);
        ((View) btStartCommunication.getParent()).setVisibility(View.GONE);
        if (peersAdapter != null) {
            peersAdapter.clear();
        }
    }

    class P2pReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.ACTION_PEERS_AVAILABLE.equals(intent.getAction())) {
                WifiP2pDeviceList wifiP2pDeviceList = intent.getParcelableExtra(Constant.KEY_PEERS_AVAILABLE);
                if (peersAdapter != null) {
                    peersAdapter.setWifiP2pDeviceList(wifiP2pDeviceList);
                } else {
                    peersAdapter = new PeersAdapter(wifiP2pDeviceList);
                    lvPeers.setAdapter(peersAdapter);
                }
            } else if (Constant.ACTION_CONNECTION_INFO.equals(intent.getAction())) {
                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(Constant.KEY_CONNECTION_INFO);
                tvInfo.setVisibility(View.VISIBLE);
                if (wifiP2pInfo.isGroupOwner) {
                    tvInfo.setText("Acts as a Server");
                } else {
                    tvInfo.setText("Acts as a Client");
                }
                ((View) btStartCommunication.getParent()).setVisibility(View.VISIBLE);
            } else if (Constant.ACTION_CONNECTION_DISCONNECTED.equals(intent.getAction())) {
                reset();
            }
        }
    }

}
