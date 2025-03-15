package com.example.hospitalassessment.models;

public class Insurance extends BaseEntity {
    private String company;
    private String address;
    private String phone;

    // Constructor to initialize insurance details
    public Insurance(String id, String company, String address, String phone) {
        super(id);
        this.company = company;
        this.address = address;
        this.phone = phone;
    }

    // Getters and setters
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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

    @Override
    public String toString() {
        // Returns a readable string representation of the insurance
        return "Insurance{" +
                "company='" + company + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
