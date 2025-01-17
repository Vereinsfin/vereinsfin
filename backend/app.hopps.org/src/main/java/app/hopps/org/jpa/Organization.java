package app.hopps.org.jpa;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Entity
@Schema(name = "Organization", description = "An example of a Hopps Organization, i.e. Verein")
public class Organization extends PanacheEntity {

    @NotBlank
    @Schema(examples = "Raketenfreunde e.V.")
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$")
    @Schema(examples = "raketen-freunde")
    private String slug;

    @NotNull
    @Schema(examples = "EINGETRAGENER_VEREIN")
    private TYPE type;

    @Embedded
    private Address address;

    @OneToOne(cascade = { CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE })
    @Schema(examples = "null")
    private Bommel rootBommel;

    @ManyToMany(mappedBy = "organizations", cascade = CascadeType.PERSIST)
    @Schema(examples = "[]")
    private Set<Member> members = new HashSet<>();

    @Schema(examples = "https://raketenfreunde.tld")
    private URL website;

    @Schema(examples = "https://example.com/avatar.png")
    private URL profilePicture;

    public Organization() {
        // no args constructor
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public URL getWebsite() {
        return website;
    }

    public void setWebsite(URL website) {
        this.website = website;
    }

    public URL getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(URL profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Set<Member> getMembers() {
        return members;
    }

    public void setMembers(Set<Member> members) {
        this.members = members;
    }

    public void addMember(Member member) {
        this.members.add(member);
    }

    public Bommel getRootBommel() {
        return rootBommel;
    }

    public void setRootBommel(Bommel rootBommel) {
        this.rootBommel = rootBommel;
    }

    public enum TYPE {

        EINGETRAGENER_VEREIN {
            @Override
            public String getDisplayString() {
                return "e.V.";
            }
        };

        // for now, we will only support e.V.

        public abstract String getDisplayString();
    }
}
