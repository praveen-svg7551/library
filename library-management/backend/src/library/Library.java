package library;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Core business logic. Uses Collections (LinkedHashMap, ArrayList),
 * file I/O for persistence and custom exceptions for validation.
 */
public class Library {
    private final Map<Integer, Book> books = new LinkedHashMap<>();
    private final Map<Integer, Member> members = new LinkedHashMap<>();
    private final Path file;
    private int nextBookId = 1, nextMemberId = 1;

    public Library(Path file) {
        this.file = file;
        load();
        if (books.isEmpty() && members.isEmpty()) seed();
    }

    // ---------- Books ----------
    public synchronized Book addBook(String title, String author, int copies) throws LibraryException {
        if (title == null || title.isBlank() || author == null || author.isBlank())
            throw new LibraryException("Title and author are required.");
        if (copies < 1 || copies > 100) throw new LibraryException("Copies must be between 1 and 100.");
        Book b = new Book(nextBookId++, clean(title), clean(author), copies, copies);
        books.put(b.getId(), b);
        save();
        return b;
    }

    public synchronized void removeBook(int id) throws LibraryException {
        Book b = findBook(id);
        if (b.getAvailable() < b.getTotal())
            throw new LibraryException("Cannot remove: some copies are currently issued.");
        books.remove(id);
        save();
    }

    // ---------- Members ----------
    public synchronized Member addMember(String name) throws LibraryException {
        if (name == null || name.isBlank()) throw new LibraryException("Member name is required.");
        Member m = new Member(nextMemberId++, clean(name));
        members.put(m.getId(), m);
        save();
        return m;
    }

    // ---------- Issue / Return ----------
    public synchronized void issue(int memberId, int bookId) throws LibraryException {
        Member m = findMember(memberId);
        Book b = findBook(bookId);
        if (m.getBorrowed().contains(bookId)) throw new LibraryException(m.getName() + " already has this book.");
        if (m.getBorrowed().size() >= Member.MAX_BOOKS)
            throw new LibraryException("Borrow limit reached (" + Member.MAX_BOOKS + " books).");
        if (b.getAvailable() < 1) throw new LibraryException("No copies available for '" + b.getTitle() + "'.");
        b.setAvailable(b.getAvailable() - 1);
        m.getBorrowed().add(bookId);
        save();
    }

    public synchronized void returnBook(int memberId, int bookId) throws LibraryException {
        Member m = findMember(memberId);
        Book b = findBook(bookId);
        if (!m.getBorrowed().remove(Integer.valueOf(bookId)))
            throw new LibraryException(m.getName() + " has not borrowed this book.");
        b.setAvailable(b.getAvailable() + 1);
        save();
    }

    // ---------- JSON ----------
    public synchronized String booksJson() {
        StringJoiner j = new StringJoiner(",", "[", "]");
        for (Book b : books.values()) j.add(b.toJson());
        return j.toString();
    }

    public synchronized String membersJson() {
        StringJoiner j = new StringJoiner(",", "[", "]");
        for (Member m : members.values()) j.add(m.toJson());
        return j.toString();
    }

    public static String q(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': break;
                default: sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    // ---------- Helpers ----------
    private Book findBook(int id) throws LibraryException {
        Book b = books.get(id);
        if (b == null) throw new LibraryException("Book #" + id + " not found.");
        return b;
    }

    private Member findMember(int id) throws LibraryException {
        Member m = members.get(id);
        if (m == null) throw new LibraryException("Member #" + id + " not found.");
        return m;
    }

    private static String clean(String s) { return s.trim().replace("|", "/").replaceAll("\\s+", " "); }

    // ---------- File persistence ----------
    private void save() {
        List<String> lines = new ArrayList<>();
        for (Book b : books.values())
            lines.add("B|" + b.getId() + "|" + b.getTitle() + "|" + b.getAuthor() + "|" + b.getTotal() + "|" + b.getAvailable());
        for (Member m : members.values()) {
            StringJoiner ids = new StringJoiner(",");
            for (int id : m.getBorrowed()) ids.add(String.valueOf(id));
            lines.add("M|" + m.getId() + "|" + m.getName() + "|" + ids);
        }
        try { Files.write(file, lines, StandardCharsets.UTF_8); }
        catch (IOException e) { System.err.println("Could not save data: " + e.getMessage()); }
    }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] p = line.split("\\|", -1);
                if (p[0].equals("B") && p.length >= 6) {
                    Book b = new Book(Integer.parseInt(p[1]), p[2], p[3], Integer.parseInt(p[4]), Integer.parseInt(p[5]));
                    books.put(b.getId(), b);
                    nextBookId = Math.max(nextBookId, b.getId() + 1);
                } else if (p[0].equals("M") && p.length >= 4) {
                    Member m = new Member(Integer.parseInt(p[1]), p[2]);
                    if (!p[3].isEmpty()) for (String id : p[3].split(",")) m.getBorrowed().add(Integer.parseInt(id));
                    members.put(m.getId(), m);
                    nextMemberId = Math.max(nextMemberId, m.getId() + 1);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Could not read data file: " + e.getMessage());
        }
    }

    private void seed() {
        try {
            addBook("Head First Java", "Kathy Sierra", 3);
            addBook("Effective Java", "Joshua Bloch", 2);
            addBook("Clean Code", "Robert C. Martin", 2);
            addBook("Java: The Complete Reference", "Herbert Schildt", 4);
            addMember("Praveen Thiruveedi");
        } catch (LibraryException ignored) { }
    }
}
