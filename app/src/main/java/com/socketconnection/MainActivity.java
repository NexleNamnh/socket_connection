package com.socketconnection;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private TCPServer mServer;
    private TCPClient mClient;
    private EditText mEdServerPort;
    private EditText mEdServerAddress;
    private ConnectTask mConnectTask;
    private TextView mTvMessageFromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckBox cbMarkServer = (CheckBox) findViewById(R.id.cb_mark_as_server);
        final LinearLayout llServerInfo = (LinearLayout) findViewById(R.id.ll_server_info);
        final LinearLayout llClientInfo = (LinearLayout) findViewById(R.id.ll_client_info);
        final TextView tvServerAddress = (TextView) findViewById(R.id.tv_ip_address);
        final TextView tvServerPort = (TextView) findViewById(R.id.tv_port);
        final TextView tvMessageFromClient = (TextView) findViewById(R.id.tv_message_from_client);
        final EditText edMessageToServer = (EditText) findViewById(R.id.et_message_to_server);
        final EditText edMessageToClient = (EditText) findViewById(R.id.et_message_to_client);
        Button btsendToClient = (Button) findViewById(R.id.bt_send_to_client);
        Button btSendToServer = (Button) findViewById(R.id.bt_send_to_server);
        Button btStartServer = (Button) findViewById(R.id.bt_start_server);
        Button btConnect = (Button) findViewById(R.id.bt_connect);
        mTvMessageFromServer = (TextView) findViewById(R.id.tv_message_from_server);
        mEdServerAddress = (EditText) findViewById(R.id.et_ip_address);
        mEdServerPort = (EditText) findViewById(R.id.et_port);

        cbMarkServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llServerInfo.setVisibility(View.VISIBLE);
                    llClientInfo.setVisibility(View.GONE);
                    tvServerAddress.setText("IP Address: " + TCPServer.getIpAddress());
                    tvServerPort.setText("Port: " + TCPServer.getPort());
                } else {
                    llServerInfo.setVisibility(View.GONE);
                    llClientInfo.setVisibility(View.VISIBLE);

                }
            }
        });
        btsendToClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServer != null) {
                    mServer.sendMessage(edMessageToClient.getText().toString());
                }
            }
        });
        btSendToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClient != null) {
                    mClient.sendMessage(edMessageToServer.getText().toString());
                }
            }
        });
        btStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServer = new TCPServer(new TCPServer.OnMessageReceived() {
                    @Override
                    public void messageReceived(final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessageFromClient.setText(message);
                            }
                        });
                    }

                    @Override
                    public void onServerStart(final boolean success) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (success) {
                                    Toast.makeText(MainActivity.this, "Start successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Start failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                mServer.start();
            }
        });
        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectTask = new ConnectTask(mEdServerAddress.getText().toString(), mEdServerPort.getText().toString());
                mConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClient != null) {
            mClient.stopClient();
        }
    }

    public class ConnectTask extends AsyncTask<String, String, TCPClient> {

        String mServerAddress;
        int mServerPort = TCPServer.getPort();

        public ConnectTask(String ipAddress, String port) {
            mServerAddress = ipAddress;
            try {
                mServerPort = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected TCPClient doInBackground(String... message) {
            //we create a TCPClient object and
            mClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(final String message) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvMessageFromServer.setText(message);
                        }
                    });
                }

                @Override
                public void onClientConnect(final boolean success) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (success) {
                                Toast.makeText(MainActivity.this, "Connection successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }, mServerAddress, mServerPort);
            mClient.run();
            return null;
        }
    }
}
