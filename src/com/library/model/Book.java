package com.library.model;

public class Book {
    private final int id;
    private String title;
    private String author;
    private String category;

    public Book(int id, String title, String author, String category) {
        if (id <= 0) throw new IllegalArgumentException("Book ID must be positive.");
        this.id = id;
        setTitle(title);
        setAuthor(author);
        setCategory(category);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }

    public void setTitle(String title) { this.title = validate(title, "Title"); }
    public void setAuthor(String author) { this.author = validate(author, "Author"); }

    public void setCategory(String category) {
        if (!category.equals("Programming") &&
            !category.equals("Science") &&
            !category.equals("Literature") &&
            !category.equals("Other")) {
            throw new IllegalArgumentException("Invalid category.");
        }
        this.category = category;
    }

    private String validate(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " cannot be blank.");
        if (value.contains("|") || value.contains("\n") || value.contains("\r"))
            throw new IllegalArgumentException(field + " contains invalid characters.");
        return value.trim();
    }

    public void showDetails(boolean available) {
        System.out.printf("%d | %s | %s | %s | %s%n",
                id, title, author, category, available ? "Available" : "Currently issued");
    }
}
