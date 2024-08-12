package com.anshu.insurancenotifier;

public class PDFData {
    private String name;
    private String renewalDate;

    public PDFData() {
        // Default constructor required for calls to DataSnapshot.getValue(PDFData.class)
    }

    public PDFData(String name, String renewalDate) {
        this.name = name;
        this.renewalDate = renewalDate;
    }

    public String getName() {
        return name;
    }

    public String getRenewalDate() {
        return renewalDate;
    }
}
