package com.library.model;

public class Librarian extends User {
    public Librarian() {
        super(0, "Librarian");
    }

    @Override
    public void showProfile() {
        System.out.println("Librarian ID: " + getId());
        System.out.println("Name: " + getName());
    }
}
