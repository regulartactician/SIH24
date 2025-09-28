package com.example.jaldisih2;

import android.net.Uri;

public class BillItem {
    private String slno;
    private String serviceName;
    private String date;
    private String cost;


    public BillItem(String slno, String serviceName, String date, String cost) {
        this.slno = slno;
        this.serviceName = serviceName;
        this.date = date;
        this.cost = cost;
    }

    public String getSlno() {
        return slno;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDate() {
        return date;
    }

    public String getCost() {
        return cost;
    }
}

