package exception;

/**
 * Custom checked exception thrown when an attempt is made to register
 * a student with an ID that already exists in the system.
 */
public class DuplicateStudentException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String studentId;

    public DuplicateStudentException(String studentId) {
        super("A student with ID '" + studentId + "' already exists in the system.");
        this.studentId = studentId;
    }

    public DuplicateStudentException(String studentId, String customMessage) {
        super(customMessage);
        this.studentId = studentId;
    }

    public String getStudentId() {
        return studentId;
    }
}
