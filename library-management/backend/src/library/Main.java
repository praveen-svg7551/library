package library;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Tiny REST API built on the JDK's built-in HttpServer (no external libraries). */
public class Main {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Library lib = new Library(Path.of(System.getenv().getOrDefault("DATA_FILE", "library-data.txt")));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/", ex -> {
            try {
                ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }

                Map<String, String> p = params(ex);
                String route = ex.getRequestMethod() + " " + ex.getRequestURI().getPath();
                String out;
                int code = 200;
                switch (route) {
                    case "GET /api/health":  out = "{\"status\":\"ok\"}"; break;
                    case "GET /api/books":   out = lib.booksJson(); break;
                    case "GET /api/members": out = lib.membersJson(); break;
                    case "POST /api/books":
                        lib.addBook(p.get("title"), p.get("author"), num(p.getOrDefault("copies", "1")));
                        out = ok(); break;
                    case "POST /api/members":
                        lib.addMember(p.get("name")); out = ok(); break;
                    case "POST /api/delete":
                        lib.removeBook(num(p.get("bookId"))); out = ok(); break;
                    case "POST /api/issue":
                        lib.issue(num(p.get("memberId")), num(p.get("bookId"))); out = ok(); break;
                    case "POST /api/return":
                        lib.returnBook(num(p.get("memberId")), num(p.get("bookId"))); out = ok(); break;
                    default: code = 404; out = err("Not found");
                }
                send(ex, code, out);
            } catch (LibraryException e) {
                send(ex, 400, err(e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                send(ex, 500, err("Server error"));
            }
        });
        server.start();
        System.out.println("Library API running on port " + port);
    }

    private static int num(String s) throws LibraryException {
        try { return Integer.parseInt(s == null ? "" : s.trim()); }
        catch (NumberFormatException e) { throw new LibraryException("Invalid number: " + s); }
    }

    private static String ok() { return "{\"ok\":true}"; }
    private static String err(String m) { return "{\"error\":" + Library.q(m) + "}"; }

    private static Map<String, String> params(HttpExchange ex) throws IOException {
        Map<String, String> map = new HashMap<>();
        String q = ex.getRequestURI().getRawQuery();
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        for (String s : new String[]{q, body}) {
            if (s == null || s.isEmpty()) continue;
            for (String pair : s.split("&")) {
                int i = pair.indexOf('=');
                if (i < 0) continue;
                map.put(URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static void send(HttpExchange ex, int code, String json) throws IOException {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, b.length);
        ex.getResponseBody().write(b);
        ex.close();
    }
}
