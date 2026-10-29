package library;

import java.util.ArrayList;
import java.util.List;

/** Library member who can borrow up to MAX_BOOKS books. */
public class Member {
    public static final int MAX_BOOKS = 3;
    private final int id;
    private final String name;
    private final List<Integer> borrowed = new ArrayList<>();

    public Member(int id, String name) { this.id = id; this.name = name; }
    public int getId() { return id; }
    public String getName() { return name; }
    public List<Integer> getBorrowed() { return borrowed; }

    public String toJson() {
        StringBuilder sb = new StringBuilder("{\"id\":" + id + ",\"name\":" + Library.q(name) + ",\"borrowed\":[");
        for (int i = 0; i < borrowed.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(borrowed.get(i));
        }
        return sb.append("]}").toString();
    }
}
