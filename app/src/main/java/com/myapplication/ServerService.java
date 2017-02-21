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
import android.support.v4.content.LocalBroadcastManager;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerService extends IntentService {

    private static String clientAddress;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ServerService() {
        super("ServerService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ServerSocket serverSocket;
        Socket socket;
        try {
            serverSocket = new ServerSocket(Constant.SERVER_PORT);
            while (true) {
                //Listen for incoming connections on specified port
                //Block thread until someone connects
                socket = serverSocket.accept();
                clientAddress = socket.getInetAddress().getHostAddress();

                InputStream is = socket.getInputStream();

                String receivedMessage = CommonUtils.getStringFromInputStream(is);
                CommonUtils.showToast(receivedMessage);
                Intent messageIntent = new Intent(Constant.ACTION_MESSAGE_FROM_PEER);
                messageIntent.putExtra(Constant.KEY_MESSAGE_FROM_PEER, receivedMessage);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);

                socket.close();
            }
        } catch (Exception e) {
            CommonUtils.showToast(e.getMessage());
        }
    }

    public static String getClientAddress() {
        return clientAddress;
    }

}
