package library;

/** Custom checked exception for business-rule violations (Week 5: exception handling). */
public class LibraryException extends Exception {
    public LibraryException(String message) { super(message); }
}
