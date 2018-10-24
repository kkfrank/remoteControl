package com.frank.remotecontrol.model;

import java.util.List;

public class Massage {
    private String name;
    //private List<String> orders;//顺序

    private String orders;
    private String interval;//间隔
    private String intensitt;//强度

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrders() {
        return orders;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getIntensitt() {
        return intensitt;
    }

    public void setIntensitt(String intensitt) {
        this.intensitt = intensitt;
    }
}
