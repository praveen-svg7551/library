package library;

/** Encapsulated Book entity (Week 4: OOP, getters/setters). */
public class Book {
    private final int id;
    private final String title;
    private final String author;
    private final int total;
    private int available;

    public Book(int id, String title, String author, int total, int available) {
        this.id = id; this.title = title; this.author = author;
        this.total = total; this.available = available;
    }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getTotal() { return total; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }

    public String toJson() {
        return "{\"id\":" + id + ",\"title\":" + Library.q(title) + ",\"author\":" + Library.q(author)
             + ",\"total\":" + total + ",\"available\":" + available + "}";
    }
}
