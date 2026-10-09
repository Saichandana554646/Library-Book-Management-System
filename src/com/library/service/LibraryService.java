package com.library.service;

import com.library.exception.*;
import com.library.model.*;
import com.library.repository.FileManager;

import java.io.IOException;
import java.util.*;

public class LibraryService {
    private final List<Member> members;
    private final List<Book> books;
    private final List<Loan> loans;
    private final FileManager fileManager;

    public static final String[] CATEGORIES = {
            "Programming", "Science", "Literature", "Other"
    };

    public LibraryService(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;
        this.members = fileManager.loadMembers();
        this.books = fileManager.loadBooks();
        this.loans = fileManager.loadLoans(members, books);
    }

    public Member registerMember(String name, String department) throws IOException {
        int id = nextIdMember();
        Member member = new Member(id, name, department);
        members.add(member);
        try {
            fileManager.saveMembers(members);
        } catch (IOException e) {
            members.remove(member);
            throw e;
        }
        return member;
    }

    public Book addBook(String title, String author, String category) throws IOException {
        int id = nextIdBook();
        Book book = new Book(id, title, author, category);
        books.add(book);
        try {
            fileManager.saveBooks(books);
        } catch (IOException e) {
            books.remove(book);
            throw e;
        }
        return book;
    }

    public Member findMember(int id) throws MemberNotFoundException {
        return members.stream().filter(m -> m.getId() == id).findFirst()
                .orElseThrow(() -> new MemberNotFoundException("Member not found."));
    }

    public Book findBook(int id) throws BookNotFoundException {
        return books.stream().filter(b -> b.getId() == id).findFirst()
                .orElseThrow(() -> new BookNotFoundException("Book not found."));
    }

    public Loan findLoan(int id) {
        return loans.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
    }

    public boolean isBookIssued(int bookId) {
        return loans.stream().anyMatch(l -> l.getBookId() == bookId && l.getStatus() == LoanStatus.ISSUED);
    }

    public int activeLoansForMember(int memberId) {
        return (int) loans.stream()
                .filter(l -> l.getMemberId() == memberId && l.getStatus() == LoanStatus.ISSUED)
                .count();
    }

    public Loan issueBook(int memberId, int bookId)
            throws IOException, MemberNotFoundException, BookNotFoundException, BookNotAvailableException {

        findMember(memberId);
        findBook(bookId);

        if (isBookIssued(bookId))
            throw new BookNotAvailableException("Book is already issued.");

        if (activeLoansForMember(memberId) >= 2)
            throw new BookNotAvailableException("A member can have a maximum of two active books.");

        Loan loan = new Loan(nextIdLoan(), memberId, bookId, LoanStatus.ISSUED);
        loans.add(loan);
        try {
            fileManager.saveLoans(loans);
        } catch (IOException e) {
            loans.remove(loan);
            throw e;
        }
        return loan;
    }

    public void returnBook(int loanId) throws IOException {
        Loan loan = findLoan(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan not found.");
        if (loan.getStatus() == LoanStatus.RETURNED)
            throw new IllegalStateException("Loan has already been returned.");

        loan.markReturned();
        try {
            fileManager.saveLoans(loans);
        } catch (IOException e) {
            // Restore state if persistence fails.
            // Recreate the only allowed reverse transition.
            try {
                java.lang.reflect.Field field = Loan.class.getDeclaredField("status");
                field.setAccessible(true);
                field.set(loan, LoanStatus.ISSUED);
            } catch (ReflectiveOperationException ignored) {
                throw new IOException("Save failed and state could not be restored.", e);
            }
            throw e;
        }
    }

    public List<Member> getMembers() { return Collections.unmodifiableList(members); }
    public List<Book> getBooks() { return Collections.unmodifiableList(books); }
    public List<Loan> getLoans() { return Collections.unmodifiableList(loans); }

    public List<Loan> getMemberLoans(int memberId) {
        return loans.stream().filter(l -> l.getMemberId() == memberId).toList();
    }

    public List<String> buildReport() {
        int issued = (int) loans.stream().filter(l -> l.getStatus() == LoanStatus.ISSUED).count();
        int returned = (int) loans.stream().filter(l -> l.getStatus() == LoanStatus.RETURNED).count();
        int available = books.size() - issued;

        HashSet<String> authors = new HashSet<>();
        for (Book book : books) {
            authors.add(book.getAuthor().trim().toUpperCase(Locale.ROOT));
        }

        return List.of(
                "LIBRARY BOOK MANAGEMENT REPORT",
                "Total members: " + members.size(),
                "Total book copies: " + books.size(),
                "Books currently issued: " + issued,
                "Available copies: " + available,
                "Total loans: " + loans.size(),
                "Returned loans: " + returned,
                "Distinct authors: " + authors.size()
        );
    }

    public void saveReport() throws IOException {
        fileManager.saveReport(buildReport());
    }

    private int nextIdMember() {
        return members.stream().mapToInt(Member::getId).max().orElse(0) + 1;
    }

    private int nextIdBook() {
        return books.stream().mapToInt(Book::getId).max().orElse(0) + 1;
    }

    private int nextIdLoan() {
        return loans.stream().mapToInt(Loan::getId).max().orElse(0) + 1;
    }
}
