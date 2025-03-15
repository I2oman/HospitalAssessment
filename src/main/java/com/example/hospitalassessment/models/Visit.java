package com.example.hospitalassessment.models;

import java.sql.Date;

public class Visit {
    private Patient patient;
    private Doctor doctor;
    private Date dateOfVisit;
    private String symptoms;
    private String diagnosis;

    public Visit(Patient patient, Doctor doctor, Date dateOfVisit, String symptoms, String diagnosis) {
        this.patient = patient;
        this.doctor = doctor;
        this.dateOfVisit = dateOfVisit;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Date getDateOfVisit() {
        return dateOfVisit;
    }

    public void setDateOfVisit(Date dateOfVisit) {
        this.dateOfVisit = dateOfVisit;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    @Override
    public String toString() {
        return "Visit{" +
                "patient=" + patient +
                ", doctor=" + doctor +
                ", dateOfVisit=" + dateOfVisit +
                ", symptoms='" + symptoms + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                '}';
    }
}
