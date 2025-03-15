package com.example.hospitalassessment.models;

public abstract class BaseEntity {
    protected String id; // Unique identifier

    public BaseEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        // Returns a simple string representation with class name and ID
        return "BaseEntity{" +
                "id='" + id + '\'' +
                '}';
    }
}
