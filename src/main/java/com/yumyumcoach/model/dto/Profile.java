package com.yumyumcoach.model.dto;

public class Profile {
    private String username;
    private String name;
    private double height;
    private double weight;
    private String disease;

    public Profile() {
    }

    public Profile(String username, String name, double height, double weight, String disease) {
        this.username = username;
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.disease = disease;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }
}
