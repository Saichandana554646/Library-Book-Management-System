package com.library.model;

public class Loan {
    private final int id;
    private final int memberId;
    private final int bookId;
    private LoanStatus status;

    public Loan(int id, int memberId, int bookId, LoanStatus status) {
        if (id <= 0 || memberId <= 0 || bookId <= 0)
            throw new IllegalArgumentException("IDs must be positive.");
        this.id = id;
        this.memberId = memberId;
        this.bookId = bookId;
        this.status = status;
    }

    public int getId() { return id; }
    public int getMemberId() { return memberId; }
    public int getBookId() { return bookId; }
    public LoanStatus getStatus() { return status; }

    public void markReturned() {
        if (status == LoanStatus.RETURNED)
            throw new IllegalStateException("Loan is already returned.");
        status = LoanStatus.RETURNED;
    }
}
