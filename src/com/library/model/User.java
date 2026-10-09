package com.library.model;

public abstract class User {
    private final int id;
    private String name;

    public User(int id, String name) {
        if (id < 0) throw new IllegalArgumentException("ID cannot be negative.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be blank.");
        if (name.contains("|") || name.contains("\n") || name.contains("\r"))
            throw new IllegalArgumentException("Name contains invalid characters.");
        this.id = id;
        this.name = name.trim();
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be blank.");
        if (name.contains("|") || name.contains("\n") || name.contains("\r"))
            throw new IllegalArgumentException("Name contains invalid characters.");
        this.name = name.trim();
    }

    public abstract void showProfile();
}
