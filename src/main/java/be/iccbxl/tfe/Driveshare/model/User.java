package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
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

    @Column(name="date_naissance")
    private Date dateNaissance;

    @Column(name="password")
    private String password;

    @Column(name="photo_profil")
    private String photoProfil;

    @Column(name="permis_conduire")
    private String permisConduire;

    @Column(name="carte_identite")
    private String carteIdentite;

    @OneToMany(mappedBy = "user")
    private List<Evaluation> evaluations;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "UserID"),
            inverseJoinColumns = @JoinColumn(name = "RoleID")
    )
    private List<Role> roles;

}
