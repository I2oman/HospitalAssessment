package com.example.hospitalassessment.models;

public class Doctor extends BaseEntity {
    private String firstName;
    private String surname;
    private String address;
    private String email;
    private String specialization;
    private String hospital;

    public Doctor(String id, String firstName, String surname, String address, String email, String specialization, String hospital) {
        super(id);
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.email = email;
        this.specialization = specialization;
        this.hospital = hospital;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", specialization='" + specialization + '\'' +
                ", hospital='" + hospital + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
