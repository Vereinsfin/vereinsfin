package app.hopps.org.rest;

import io.quarkiverse.openfga.client.AuthorizationModelClient;
import io.quarkiverse.openfga.client.model.ConditionalTupleKey;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class FgaBommelResourceTest {
    @Inject
    AuthorizationModelClient authorizationModelClient;

    @Test
    @TestSecurity(user = "test")
    void shouldReadChildren() {
        authorizationModelClient.write(ConditionalTupleKey.of("bommel:1", "read", "user:test")).await().atMost(Duration.ofSeconds(2));

        given()
                .when()
                .get("bommel/1/children")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "peter")
    void shouldBeForbiddenToReadChildren() {
        given()
                .when()
                .get("bommel/1/children")
                .then()
                .statusCode(403);
    }
}
