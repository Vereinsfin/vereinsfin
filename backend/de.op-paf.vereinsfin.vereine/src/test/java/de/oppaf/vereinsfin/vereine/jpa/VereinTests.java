package de.oppaf.vereinsfin.vereine.jpa;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.narayana.jta.QuarkusTransactionException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.RollbackException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class VereinTests {

    @Inject
    VereinRepository vereinRepository;

    @Inject
    Validator validator;

    @Test
    @DisplayName("should persist a verein")
    @TestTransaction
    void shouldPersistVerein() {

        // given
        Verein verein = Instancio.create(Verein.class);
        verein.setId(null);

        // when
        vereinRepository.persist(verein);

        // then
        assertThat(vereinRepository.listAll(), hasSize(1));
        assertNotNull(vereinRepository.listAll().getFirst().getName());
    }

    @Test
    @DisplayName("should have an embedded address")
    @TestTransaction
    void shouldHaveEmbeddedAddress() {

        // Caveat: @TestTransaction will not trigger the Hibernate Validation

        // given
        Verein verein = Instancio.create(Verein.class);
        verein.setId(null);

        // when
        vereinRepository.persist(verein);

        // then
        Address address = vereinRepository.listAll().getFirst().getAddress();
        assertThat(address.getStreet(), not(emptyOrNullString()));
        assertThat(address.getNumber(), not(emptyOrNullString()));
    }

    @Test
    @DisplayName("must enforce a non blank name")
    void mustEnforceANonBlankName() {

        // given
        Verein verein = Instancio.create(Verein.class);
        verein.setId(null);
        verein.setName("");

        // when
        Set<ConstraintViolation<Verein>> validations = validator.validate(verein);

        // then
        assertThat(validations, hasSize(1));

        ConstraintViolation<Verein> validation = validations.stream().findFirst().orElseThrow();
        assertEquals("name", validation.getPropertyPath().toString());
        assertEquals("must not be blank", validation.getMessage());
    }

    @Test
    @DisplayName("must not persist with a blank name and understand the exceptions")
    void mustNotPersistWithABlankName() {

        // given
        Verein verein = Instancio.create(Verein.class);
        verein.setId(null);
        verein.setName("");

        // when
        QuarkusTransaction.begin();
        vereinRepository.persist(verein);
        try {
            QuarkusTransaction.commit();
        } catch (Exception e) {
            // then
            assertThat(e, instanceOf(QuarkusTransactionException.class));
            assertThat(e.getCause(), instanceOf(RollbackException.class));
            assertThat(e.getCause().getCause(), instanceOf(ConstraintViolationException.class));
            // yes, this is quite a complicated cascade
        }
    }
}
