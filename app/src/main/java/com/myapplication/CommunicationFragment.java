package com.myapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommunicationFragment extends BaseFragment {

    private MainActivity activity;
    private P2pReceiver p2pReceiver;
    private TextView tvReceivedMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                activity.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_communication, container, false);
        }
        final EditText edMessage = (EditText) rootView.findViewById(R.id.ed_message);
        Button btSendMessage = (Button) rootView.findViewById(R.id.bt_send_message);
        tvReceivedMessage = (TextView) rootView.findViewById(R.id.tv_message_from_peer);
        btSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = edMessage.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    WifiP2pInfo wifiP2pInfo = WifiDirectBroadcastReceiver.wifiP2pInfo;
                    Intent clientIntent = new Intent(activity, ClientService.class);
                    clientIntent.putExtra(Constant.KEY_MESSAGE_TO_SEND, message);
                    if (wifiP2pInfo.isGroupOwner) {
                        clientIntent.putExtra(Constant.KEY_CLIENT_ADDRESS, ServerService.getClientAddress());
                    } else {
                        clientIntent.putExtra(Constant.KEY_CLIENT_ADDRESS, wifiP2pInfo.groupOwnerAddress.getHostAddress());
                    }
                    activity.startService(clientIntent);
                } else {
                    CommonUtils.showToast("Please enter your message");
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_MESSAGE_FROM_PEER);
        p2pReceiver = new P2pReceiver();
        LocalBroadcastManager.getInstance(activity).registerReceiver(p2pReceiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(p2pReceiver);
    }

    class P2pReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.ACTION_MESSAGE_FROM_PEER.equals(intent.getAction())) {
                final String message = intent.getStringExtra(Constant.KEY_MESSAGE_FROM_PEER);
                tvReceivedMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        tvReceivedMessage.setText(message);
                    }
                });
            }
        }
    }
}
