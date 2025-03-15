package com.example.hospitalassessment.models;

public class Patient extends BaseEntity {
    private String firstName;
    private String surname;
    private String postcode;
    private String address;
    private String phone;
    private String email;
    private Insurance insurance;

    public Patient(String id, String firstName, String surname, String postcode, String address, String phone, String email, Insurance insurance) {
        super(id);
        this.firstName = firstName;
        this.surname = surname;
        this.postcode = postcode;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.insurance = insurance;
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

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Insurance getInsurance() {
        return insurance;
    }

    public void setInsurance(Insurance insurance) {
        this.insurance = insurance;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", postcode='" + postcode + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", insurance=" + insurance +
                ", id='" + id + '\'' +
                '}';
    }
}
