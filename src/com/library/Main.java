package com.library;

import com.library.exception.*;
import com.library.model.*;
import com.library.repository.FileManager;
import com.library.service.LibraryService;

import java.io.IOException;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static LibraryService service;

    public static void main(String[] args) {
        try {
            service = new LibraryService(new FileManager());
        } catch (IOException e) {
            System.out.println("Startup error: " + e.getMessage());
            return;
        }

        System.out.println("========================================");
        System.out.println("     LIBRARY BOOK MANAGEMENT SYSTEM");
        System.out.println("========================================");

        while (true) {
            System.out.println("\nMAIN MENU");
            System.out.println("1. Librarian menu");
            System.out.println("2. Member menu");
            System.out.println("0. Exit");

            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> showLibrarianMenu();
                case 2 -> showMemberMenu();
                case 0 -> {
                    System.out.println("Thank you. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void showLibrarianMenu() {
        while (true) {
            System.out.println("\nLIBRARIAN MENU");
            System.out.println("1. Register member");
            System.out.println("2. Add book");
            System.out.println("3. View books");
            System.out.println("4. Issue book");
            System.out.println("5. Return book");
            System.out.println("6. View loans and reports");
            System.out.println("0. Back");

            int choice = readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1 -> registerMember();
                    case 2 -> addBook();
                    case 3 -> viewBooks();
                    case 4 -> issueBook();
                    case 5 -> returnBook();
                    case 6 -> reports();
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void showMemberMenu() {
        int memberId = readInt("Enter member ID: ");
        try {
            Member member = service.findMember(memberId);
            while (true) {
                System.out.println("\nMEMBER MENU");
                System.out.println("1. View my profile");
                System.out.println("2. View books");
                System.out.println("3. View my borrowing history");
                System.out.println("0. Back");

                int choice = readInt("Enter choice: ");
                switch (choice) {
                    case 1 -> member.showProfile();
                    case 2 -> viewBooks();
                    case 3 -> viewMemberHistory(memberId);
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void registerMember() throws IOException {
        String name = readRequired("Enter member name: ");
        String department = readRequired("Enter department: ");
        Member member = service.registerMember(name, department);
        System.out.println("Member " + member.getId() + " saved.");
    }

    private static void addBook() throws IOException {
        String title = readRequired("Title: ");
        String author = readRequired("Author: ");

        System.out.println("Categories:");
        for (int i = 0; i < LibraryService.CATEGORIES.length; i++)
            System.out.println((i + 1) + ". " + LibraryService.CATEGORIES[i]);

        int choice;
        do {
            choice = readInt("Category choice: ");
            if (choice < 1 || choice > LibraryService.CATEGORIES.length)
                System.out.println("Invalid category choice.");
        } while (choice < 1 || choice > LibraryService.CATEGORIES.length);

        Book book = service.addBook(title, author, LibraryService.CATEGORIES[choice - 1]);
        System.out.println("Book " + book.getId() + " saved.");
    }

    private static void viewBooks() {
        if (service.getBooks().isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.println("\nID | Title | Author | Category | Status");
        for (Book book : service.getBooks())
            book.showDetails(!service.isBookIssued(book.getId()));
    }

    private static void issueBook() throws IOException, MemberNotFoundException,
            BookNotFoundException, BookNotAvailableException {
        int memberId = readInt("Member ID: ");
        int bookId = readInt("Book ID: ");

        Loan loan = service.issueBook(memberId, bookId);
        System.out.println("Loan " + loan.getId() + " saved. Status: " + loan.getStatus());
    }

    private static void returnBook() throws IOException {
        int loanId = readInt("Return loan ID: ");
        service.returnBook(loanId);
        Loan loan = service.findLoan(loanId);
        System.out.println("Loan " + loanId + " saved. Status: " + loan.getStatus());
        System.out.println("Book " + loan.getBookId() + " is now available.");
    }

    private static void reports() throws IOException {
        System.out.println();
        for (String line : service.buildReport())
            System.out.println(line);

        String choice = readRequired("Save report to report.txt? (Y/N): ");
        if (choice.equalsIgnoreCase("Y")) {
            service.saveReport();
            System.out.println("Report saved.");
        }
    }

    private static void viewMemberHistory(int memberId) {
        List<Loan> history = service.getMemberLoans(memberId);
        if (history.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        for (Loan loan : history) {
            Book book;
            try {
                book = service.findBook(loan.getBookId());
                System.out.printf("Loan %d | Book %d | %s | %s%n",
                        loan.getId(), book.getId(), book.getTitle(), loan.getStatus());
            } catch (BookNotFoundException e) {
                System.out.println("Loan " + loan.getId() + " | Book not found | " + loan.getStatus());
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isBlank()) return value;
            System.out.println("This field cannot be blank.");
        }
    }
}
