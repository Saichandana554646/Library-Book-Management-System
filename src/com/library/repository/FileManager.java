package com.library.repository;

import com.library.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileManager {

    private final Path dataDir = Paths.get("data");
    private final Path membersFile = dataDir.resolve("members.txt");
    private final Path booksFile = dataDir.resolve("books.txt");
    private final Path loansFile = dataDir.resolve("loans.txt");
    private final Path reportFile = dataDir.resolve("report.txt");

    public FileManager() throws IOException {
        Files.createDirectories(dataDir);
    }

    public List<Member> loadMembers() throws IOException {
        List<Member> list = new ArrayList<>();

        if (!Files.exists(membersFile)) {
            return list;
        }

        Set<Integer> ids = new HashSet<>();
        int lineNo = 0;

        try (BufferedReader br = Files.newBufferedReader(
                membersFile,
                StandardCharsets.UTF_8)) {

            String line;

            while ((line = br.readLine()) != null) {
                lineNo++;

                String[] fields = line.split("\\|", -1);

                if (fields.length != 3) {
                    throw new IOException(
                            "Invalid members.txt at line " + lineNo
                    );
                }

                int id = Integer.parseInt(fields[0]);

                if (id <= 0 || !ids.add(id)) {
                    throw new IOException(
                            "Invalid or duplicate member ID at line "
                                    + lineNo
                    );
                }

                Member member = new Member(
                        id,
                        fields[1],
                        fields[2]
                );

                list.add(member);
            }

        } catch (IllegalArgumentException e) {
            throw new IOException(
                    "Invalid members.txt at line "
                            + lineNo
                            + ": "
                            + e.getMessage(),
                    e
            );
        }

        return list;
    }

    public List<Book> loadBooks() throws IOException {
        List<Book> list = new ArrayList<>();

        if (!Files.exists(booksFile)) {
            return list;
        }

        Set<Integer> ids = new HashSet<>();
        int lineNo = 0;

        try (BufferedReader br = Files.newBufferedReader(
                booksFile,
                StandardCharsets.UTF_8)) {

            String line;

            while ((line = br.readLine()) != null) {
                lineNo++;

                String[] fields = line.split("\\|", -1);

                if (fields.length != 4) {
                    throw new IOException(
                            "Invalid books.txt at line " + lineNo
                    );
                }

                int id = Integer.parseInt(fields[0]);

                if (id <= 0 || !ids.add(id)) {
                    throw new IOException(
                            "Invalid or duplicate book ID at line "
                                    + lineNo
                    );
                }

                Book book = new Book(
                        id,
                        fields[1],
                        fields[2],
                        fields[3]
                );

                list.add(book);
            }

        } catch (IllegalArgumentException e) {
            throw new IOException(
                    "Invalid books.txt at line "
                            + lineNo
                            + ": "
                            + e.getMessage(),
                    e
            );
        }

        return list;
    }

    public List<Loan> loadLoans(
            List<Member> members,
            List<Book> books) throws IOException {

        List<Loan> list = new ArrayList<>();

        if (!Files.exists(loansFile)) {
            return list;
        }

        Set<Integer> ids = new HashSet<>();
        Set<Integer> memberIds = new HashSet<>();
        Set<Integer> bookIds = new HashSet<>();

        for (Member member : members) {
            memberIds.add(member.getId());
        }

        for (Book book : books) {
            bookIds.add(book.getId());
        }

        int lineNo = 0;

        try (BufferedReader br = Files.newBufferedReader(
                loansFile,
                StandardCharsets.UTF_8)) {

            String line;

            while ((line = br.readLine()) != null) {
                lineNo++;

                String[] fields = line.split("\\|", -1);

                if (fields.length != 4) {
                    throw new IOException(
                            "Invalid loans.txt at line " + lineNo
                    );
                }

                int id = Integer.parseInt(fields[0]);
                int memberId = Integer.parseInt(fields[1]);
                int bookId = Integer.parseInt(fields[2]);
                LoanStatus status = LoanStatus.valueOf(fields[3]);

                if (id <= 0 || !ids.add(id)) {
                    throw new IOException(
                            "Invalid or duplicate loan ID at line "
                                    + lineNo
                    );
                }

                if (!memberIds.contains(memberId)) {
                    throw new IOException(
                            "Member ID "
                                    + memberId
                                    + " not found at line "
                                    + lineNo
                    );
                }

                if (!bookIds.contains(bookId)) {
                    throw new IOException(
                            "Book ID "
                                    + bookId
                                    + " not found at line "
                                    + lineNo
                    );
                }

                Loan loan = new Loan(
                        id,
                        memberId,
                        bookId,
                        status
                );

                list.add(loan);
            }

        } catch (IllegalArgumentException e) {
            throw new IOException(
                    "Invalid loans.txt at line "
                            + lineNo
                            + ": "
                            + e.getMessage(),
                    e
            );
        }

        return list;
    }

    public void saveMembers(List<Member> members)
            throws IOException {

        List<String> lines = new ArrayList<>();

        for (Member member : members) {
            String line =
                    member.getId()
                            + "|"
                            + member.getName()
                            + "|"
                            + member.getDepartment();

            lines.add(line);
        }

        writeAtomically(membersFile, lines);
    }

    public void saveBooks(List<Book> books)
            throws IOException {

        List<String> lines = new ArrayList<>();

        for (Book book : books) {
            String line =
                    book.getId()
                            + "|"
                            + book.getTitle()
                            + "|"
                            + book.getAuthor()
                            + "|"
                            + book.getCategory();

            lines.add(line);
        }

        writeAtomically(booksFile, lines);
    }

    public void saveLoans(List<Loan> loans)
            throws IOException {

        List<String> lines = new ArrayList<>();

        for (Loan loan : loans) {
            String line =
                    loan.getId()
                            + "|"
                            + loan.getMemberId()
                            + "|"
                            + loan.getBookId()
                            + "|"
                            + loan.getStatus();

            lines.add(line);
        }

        writeAtomically(loansFile, lines);
    }

    public void saveReport(List<String> lines)
            throws IOException {

        writeAtomically(reportFile, lines);
    }

    private void writeAtomically(
            Path target,
            List<String> lines) throws IOException {

        Path tempFile =
                target.resolveSibling(
                        target.getFileName() + ".tmp"
                );

        try (BufferedWriter bw = Files.newBufferedWriter(
                tempFile,
                StandardCharsets.UTF_8)) {

            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }

        Files.move(
                tempFile,
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
    }
}