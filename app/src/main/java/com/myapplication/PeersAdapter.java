package com.myapplication;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Unkown on 2/20/17.
 */

public class PeersAdapter extends BaseAdapter {

    private List<WifiP2pDevice> wifiP2pDevices;

    public PeersAdapter(WifiP2pDeviceList deviceList) {
        setWifiP2pDevices(deviceList);
    }

    @Override
    public int getCount() {
        return wifiP2pDevices == null ? 0  : wifiP2pDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return wifiP2pDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi_peers, parent, false);
        }
        final WifiP2pDevice p2pDevice = (WifiP2pDevice) getItem(position);
        if (p2pDevice != null) {
            TextView tvDeviceName = (TextView) convertView.findViewById(R.id.tv_device_name);
            TextView tvDeviceStatus = (TextView) convertView.findViewById(R.id.tv_device_status);
            tvDeviceName.setText(p2pDevice.deviceName);
            tvDeviceStatus.setText("Status: " + getDeviceStatus(p2pDevice.status));
        }
        return convertView;
    }

    public void setWifiP2pDeviceList(WifiP2pDeviceList wifiP2pDeviceList) {
        setWifiP2pDevices(wifiP2pDeviceList);
        notifyDataSetChanged();
    }

    public void clear() {
        wifiP2pDevices.clear();
        notifyDataSetChanged();
    }

    private void setWifiP2pDevices(WifiP2pDeviceList WifiP2pDeviceList) {
        wifiP2pDevices = new ArrayList<>();
        for(WifiP2pDevice device: WifiP2pDeviceList.getDeviceList()) {
            wifiP2pDevices.add(device);
        }
    }

    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }
}
