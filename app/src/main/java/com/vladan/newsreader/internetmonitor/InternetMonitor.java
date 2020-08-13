package com.vladan.newsreader.internetmonitor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vladan on 7/25/2020
 */
public class InternetMonitor extends NetworkCallback {
    private static String TAG = "InternetMonitor";
    Context context;
    ConnectivityManager mConnectivityManager;
    NetworkRequest mNetworkRequest;
    NetworkRequest.Builder mBuilder;
    WifiManager mWifiManager;
    private List<Network> mAliveNetworks = new ArrayList<>();
    private Network mLosingNetwork = null;
    private Network mAvailableNetwork = null;
    private boolean mIsConnected = false;
    private int mNetworkType = -1;
    private int mSignalStrength = 1000;

    public InternetMonitor(Context context) {
        this.context = context;
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        mAliveNetworks.add(network);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mIsConnected = true;
            NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    mNetworkType = NetworkCapabilities.TRANSPORT_WIFI;
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    mNetworkType = NetworkCapabilities.TRANSPORT_CELLULAR;
                }
            }
            deliverEvent();
        }
        mAvailableNetwork = network;
        Log.d(TAG, "Method OnAvailable:" + mAliveNetworks.toString() +
                "\nAvailableNetwork:" + mAvailableNetwork.toString() +
                "\nIs connected:" + mIsConnected);
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
        mLosingNetwork = network;
        Log.d(TAG, "Method OnLosing" +
                "\nLosing network" + network.toString());
    }

    @Override
    public void onLost(@NonNull final Network network) {
        super.onLost(network);
        new Thread() {
            @Override
            public void run() {

                if (network == mLosingNetwork) {
                    mLosingNetwork = null;
                }
                if (mAliveNetworks.size() > 0) {
                    List<Network> toRemove = new ArrayList<>();
                    for (Network aliveNetwork : mAliveNetworks) {
                        if (aliveNetwork.equals(network)) {
                            toRemove.add(network);
                            Log.d(TAG, "Method on lost added toRemove" + network.toString() + " " + Thread.currentThread().getName());
                        }
                    }
                    mAliveNetworks.removeAll(toRemove);
                    Log.d(TAG, "Method on lost removed" + toRemove.toString() + network.toString() + " " + Thread.currentThread().getName());
                }
                if (mAliveNetworks.size() == 0) {
                    mIsConnected = false;
                    mNetworkType = -1;
                    mAvailableNetwork = null;
                    mSignalStrength = 1000;
                    deliverEvent();
                }
                Log.d(TAG, "Method onLost:" +
                        "\nisConnected:" + mIsConnected +
                        "\nAlive networks:" + mAliveNetworks.toString());
            }
        }.start();
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
        Log.d(TAG, "onUnavailable is triggered");
        //TODO This method works only for API level above 25 (introduced at 26).
        if (mAliveNetworks.size() != 0)
            mAliveNetworks.clear();
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mIsConnected = (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        }

        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            mNetworkType = NetworkCapabilities.TRANSPORT_WIFI;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mSignalStrength = networkCapabilities.getSignalStrength();
            } else {
                mSignalStrength = mWifiManager.getConnectionInfo().getRssi();
            }
        }
        Log.d(TAG, "Method onCapabilitiesChanged:" +
                "\nIsConnected:" + mIsConnected +
                "\nNetwork capabilities:" + networkCapabilities.toString());
    }

    @Override
    public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties);
    }

    @Override
    public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
        super.onBlockedStatusChanged(network, blocked);
        //TODO This method works only for API level above 28 (introduced at 29).
    }

    public void registerInternetMonitor() {
        mBuilder = new NetworkRequest.Builder();
        mNetworkRequest = mBuilder.build();
        mConnectivityManager.registerNetworkCallback(mNetworkRequest, this);
    }

    private void deliverEvent() {
        EventBus.getDefault().postSticky(new NetworkState(
                mIsConnected,
                mNetworkType,
                mSignalStrength));
    }
}
