package com.example.fitsync.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private int age;
    private String gender; // 'M' or 'F'
    private double weight;
    private double height;

    // Constructor without ID (for new users)
    public User(String name, String email, String password, int age, String gender, double weight, double height) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
    }

    // Constructor with ID (for retrieving from DB)
    public User(int id, String name, String email, String password, int age, String gender, double weight, double height) {
        this(name, email, password, age, gender, weight, height);
        this.id = id;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}
