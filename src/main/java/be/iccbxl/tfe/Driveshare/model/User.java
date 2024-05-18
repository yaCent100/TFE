package be.iccbxl.tfe.Driveshare.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name="date_naissance")
    private Date dateNaissance;

    @Column(name="password")
    private String password;

    @Column(name="photo_profil")
    private String photoUrl;

    @Column(name="permis_conduire")
    private String permisConduire;

    @Column(name="carte_identite")
    private String carteIdentite;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER)
    private List<Car> ownedCars;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "UserID"),
            inverseJoinColumns = @JoinColumn(name = "RoleID")
    )
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CarRental> carRentals;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Document> documents;


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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + nom + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
