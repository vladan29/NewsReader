package com.vladan.newsreader.internetmonitor;

/**
 * Created by vladan on 7/25/2020
 */
public class NetworkState {
    boolean mIsConnected;
    int mConnectionType;
    int mSignalStrength;

    public NetworkState(boolean isConnected, int connectionType, int signalStrength) {
        mIsConnected = isConnected;
        mConnectionType = connectionType;
        mSignalStrength = signalStrength;
    }

}
