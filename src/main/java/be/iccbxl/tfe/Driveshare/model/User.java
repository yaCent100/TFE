package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="Nom")
    private String nom;

    @Column(name="Prenom")
    private String prenom;

    @Column(name="Email")
    private String email;

    @Column(name="Adresse")
    private String adresse;

    @Column(name="locality")
    private String locality;

    @Column(name="code_postal")
    private String codePostal;

    @Column(name = "telephone")
    private String telephoneNumber;

    @Column(name="password")
    private String password;

    @Column(name="profil_picture", nullable = true)
    private String photoUrl;

    @Column(name="iban", nullable = true)
    private String iban;

    @Column(name="bic", nullable = true)
    private String bic;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Car> ownedCars;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notificationsSent;

    @OneToMany(mappedBy = "toUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notificationsReceived;

    public User addRole(Role role) {
        if(!this.roles.contains(role)) {
            this.roles.add(role);
            role.getUsers().add(this);
        }

        return this;
    }

    public User removeRole(Role role) {
        if(this.roles.contains(role)) {
            this.roles.remove(role);
            role.getUsers().remove(this);
        }

        return this;
    }

    public void addDocument(Document document) {
        documents.add(document);
    }

    public void removeDocument(Document document) {
        documents.remove(document);
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + nom + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean hasIdentityDocuments() {
        boolean hasRecto = documents.stream().anyMatch(doc -> "identity_recto".equals(doc.getDocumentType()));
        boolean hasVerso = documents.stream().anyMatch(doc -> "identity_verso".equals(doc.getDocumentType()));
        return hasRecto && hasVerso;
    }


}
