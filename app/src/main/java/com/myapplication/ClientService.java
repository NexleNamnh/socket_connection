/*
 WiFi Direct File Transfer is an open source application that will enable sharing 
 of data between Android devices running Android 4.0 or higher using a WiFi direct
 connection without the use of a separate WiFi access point.This will enable data 
 transfer between devices without relying on any existing network infrastructure. 
 This application is intended to provide a much higher speed alternative to Bluetooth
 file transfers. 

 Copyright (C) 2012  Teja R. Pitla
 Contact: teja.pitla@gmail.com

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>
 */


package com.myapplication;


import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientService extends IntentService {

    private static final String TAG = ClientService.class.getSimpleName();
    private PrintWriter out;
    private Socket clientSocket;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ClientService() {
        super(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: " + TAG);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendMessage(final String message) {
        if (!TextUtils.isEmpty(message)) {
            try {
                if (out != null && !out.checkError()) {
                    System.out.println(message);
                    // Here you can connect with database or else you can do what you want with static message
                    out.println(message);
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String messgeToSend = intent.getStringExtra(Constant.KEY_MESSAGE_TO_SEND);
        if (!TextUtils.isEmpty(messgeToSend) && out != null) {
            this.sendMessage(messgeToSend);
            return;
        }

        String clientAddress = intent.getStringExtra(Constant.KEY_CLIENT_ADDRESS);
        try {
            clientSocket = new Socket();
            clientSocket.bind(null);
            clientSocket.connect((new InetSocketAddress(clientAddress, Constant.SERVER_PORT)), Constant.SOCKET_TIMEOUT);

            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                sendMessage(messgeToSend);
            } catch (Exception e) {
                CommonUtils.showToast(e.getMessage());
            } finally {
                out.flush();
                out.close();
                if (clientSocket != null) {
                    if (clientSocket.isConnected()) {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (clientSocket != null) {
            if (clientSocket.isConnected()) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        clientSocket = socket;
    }


}