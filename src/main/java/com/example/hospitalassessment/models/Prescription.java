package com.example.hospitalassessment.models;

import java.sql.Date;

public class Prescription extends BaseEntity {
    private Date datePrescribed;
    private int dosage;
    private int duration;
    private String comment;
    private Drug drug;
    private Doctor doctor;
    private Patient patient;

    public Prescription(String id, Date datePrescribed, int dosage, int duration, String comment, Drug drug, Doctor doctor, Patient patient) {
        super(id);
        this.datePrescribed = datePrescribed;
        this.dosage = dosage;
        this.duration = duration;
        this.comment = comment;
        this.drug = drug;
        this.doctor = doctor;
        this.patient = patient;
    }

    public Date getDatePrescribed() {
        return datePrescribed;
    }

    public void setDatePrescribed(Date datePrescribed) {
        this.datePrescribed = datePrescribed;
    }

    public int getDosage() {
        return dosage;
    }

    public void setDosage(int dosage) {
        this.dosage = dosage;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "datePrescribed=" + datePrescribed +
                ", dosage=" + dosage +
                ", duration=" + duration +
                ", comment='" + comment + '\'' +
                ", drug=" + drug +
                ", doctor=" + doctor +
                ", patient=" + patient +
                ", id='" + id + '\'' +
                '}';
    }
}
