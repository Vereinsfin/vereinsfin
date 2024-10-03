package app.hopps.org.bpmn;

import app.hopps.org.jpa.Member;
import app.hopps.org.jpa.Organization;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.AbstractUserRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsIterableContaining.hasItem;

@QuarkusTest
class CreateUserInKeycloakByProcessTests {

    @Inject
    @Named("NewOrganization")
    Process<? extends Model> newOrganizationProcess;

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "app.hopps.vereine.auth.realm-name")
    String realmName;

    @Test
    @DisplayName("should create user in keycloak")
    void shouldCreateUserInKeycloak() {

        //given
        Organization kegelclub = new Organization();
        kegelclub.setName("Kegelklub 777");
        kegelclub.setType(Organization.TYPE.EINGETRAGENER_VEREIN);
        kegelclub.setSlug("kegelklub-777");

        Member kevin = new Member();
        kevin.setFirstName("Kevin");
        kevin.setLastName("Kegelkönig");
        kevin.setEmail("pinking777@gmail.com");

        // when
        Model model = newOrganizationProcess.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("organization", kegelclub);
        parameters.put("owner", kevin);
        model.fromMap(parameters);

        ProcessInstance<? extends Model> instance = newOrganizationProcess.createInstance(model);
        instance.start();

        // then
        UsersResource usersResource = keycloak.realm(realmName).users();
        List<UserRepresentation> allUsers = usersResource.searchByEmail("pinking777@gmail.com", true);
        List<String> allUserNames = allUsers.stream().map(AbstractUserRepresentation::getFirstName).toList();
        assertThat(allUserNames, hasItem("Kevin"));
    }
}
