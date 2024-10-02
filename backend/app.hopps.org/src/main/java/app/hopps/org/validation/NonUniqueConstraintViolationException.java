package app.hopps.org.validation;

import java.util.Collections;
import java.util.Set;

public final class NonUniqueConstraintViolationException extends Exception {

    private final Set<NonUniqueConstraintViolation> violations;

    public NonUniqueConstraintViolationException(Set<NonUniqueConstraintViolation> violations) {
        super("not unique");
        this.violations = violations;
    }

    public Set<NonUniqueConstraintViolation> getViolations() {
        return Collections.unmodifiableSet(violations);
    }
}