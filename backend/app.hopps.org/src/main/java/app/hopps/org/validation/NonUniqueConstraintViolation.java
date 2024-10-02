package app.hopps.org.validation;

public record NonUniqueConstraintViolation(String field, Object root) {

    public String getMessage() {
        return "must be unique";
    }
}
