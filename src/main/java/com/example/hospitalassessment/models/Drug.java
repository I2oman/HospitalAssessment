package com.example.hospitalassessment.models;

public class Drug extends BaseEntity {
    private String drugName;
    private String sideEffects;
    private String benefits;

    public Drug(String id, String drugName, String sideEffects, String benefits) {
        super(id);
        this.drugName = drugName;
        this.sideEffects = sideEffects;
        this.benefits = benefits;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    @Override
    public String toString() {
        return "Drug{" +
                "drugName='" + drugName + '\'' +
                ", sideEffects='" + sideEffects + '\'' +
                ", benefits='" + benefits + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
