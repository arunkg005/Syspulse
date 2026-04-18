package com.example.sensys.model;

import java.io.Serializable;

public class NetworkInfoData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean connected;
    private final String transportLabel;
    private final String ipAddress;
    private final String detail;

    public NetworkInfoData(boolean connected, String transportLabel, String ipAddress, String detail) {
        this.connected = connected;
        this.transportLabel = transportLabel;
        this.ipAddress = ipAddress;
        this.detail = detail;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getTransportLabel() {
        return transportLabel;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDetail() {
        return detail;
    }
}
