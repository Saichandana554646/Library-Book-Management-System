package com.library.model;

public class Member extends User {
    private String department;

    public Member(int id, String name, String department) {
        super(id, name);
        setDepartment(department);
    }

    public String getDepartment() { return department; }

    public void setDepartment(String department) {
        if (department == null || department.isBlank())
            throw new IllegalArgumentException("Department cannot be blank.");
        if (department.contains("|") || department.contains("\n") || department.contains("\r"))
            throw new IllegalArgumentException("Department contains invalid characters.");
        this.department = department.trim();
    }

    @Override
    public void showProfile() {
        System.out.println("Member ID: " + getId());
        System.out.println("Name: " + getName());
        System.out.println("Department: " + department);
    }
}
