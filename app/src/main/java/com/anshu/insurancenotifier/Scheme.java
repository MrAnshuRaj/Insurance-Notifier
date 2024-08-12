package com.anshu.insurancenotifier;

public class Scheme {
    private String schemeName;
    private String renewalDate;
    private int daysRemaining;

    public Scheme(String schemeName, String renewalDate, int daysRemaining) {
        this.schemeName = schemeName;
        this.renewalDate = renewalDate;
        this.daysRemaining = daysRemaining;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public String getRenewalDate() {
        return renewalDate;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }
}
