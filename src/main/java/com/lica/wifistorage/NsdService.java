/*
Copyright 2011-2013 Pieter Pareit

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.lica.wifistorage;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NsdService extends Service {
    private static final String TAG = NsdService.class.getSimpleName();

    private NsdManager mNsdManager = null;

    public static class StartStopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.d(TAG, "onReceive broadcast: " + intent.getAction());

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                Logger.w(TAG, "onReceive: Running pre-JB, version to old for");
                Logger.w(TAG, "onReceive: NSD functionality, bailing out");
                return;
            }
            if (intent.getAction().equals(FsService.ACTION_STARTED)) {
                Intent service = new Intent(context, NsdService.class);
                context.startService(service);
            } else if (intent.getAction().equals(FsService.ACTION_STOPPED)) {
                Intent service = new Intent(context, NsdService.class);
                context.stopService(service);
            }
        }

    }

    private RegistrationListener mRegistrationListener = new RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            Logger.d(TAG, "onServiceRegistered: " + serviceInfo.getServiceName());
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Logger.d(TAG, "onServiceUnregistered: " + serviceInfo.getServiceName());
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Logger.d(TAG, "onRegistrationFailed: errorCode=" + errorCode);
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Logger.d(TAG, "onUnregistrationFailed: errorCode=" + errorCode);
        }
    };

    @Override
    public void onCreate() {
        Logger.d(TAG, "onCreate: Entered");

        Resources res = getResources();
        String serviceNamePostfix = res.getString(R.string.nsd_servername_postfix);
        String serviceName = Build.MODEL + " " + serviceNamePostfix;

        final NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType("_ftp._tcp.");
        serviceInfo.setPort(FsSettings.getPortNumber());

        new Thread(new Runnable() {
            @Override
            public void run() {
                // this call sometimes hangs, this is why I get it in a separate thread
                Logger.d(TAG, "onCreate: Trying to get the NsdManager");
                mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
                if (mNsdManager != null) {
                    Logger.d(TAG, "onCreate: Got the NsdManager");
                    try {
                        mNsdManager.registerService(serviceInfo,
                                NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
                    } catch (RuntimeException e) {
                        Logger.e(TAG, "onCreate: Failed to register NsdManager");
                        mNsdManager = null;
                    }
                } else {
                    Logger.d(TAG, "onCreate: Failed to get the NsdManager");
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand: Entered");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy: Entered");

        if (mNsdManager == null) {
            Logger.e(TAG, "unregisterService: Unexpected mNsdManger to be null, bailing out");
            return;
        }
        try {
            mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
            mNsdManager.unregisterService(mRegistrationListener);
        } catch (Exception e) {
            Logger.e(TAG, "Unable to unregister NSD service, error: " + e.getMessage());
        }
        mNsdManager = null;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
