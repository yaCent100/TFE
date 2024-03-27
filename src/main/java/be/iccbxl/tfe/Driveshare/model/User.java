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

    @Column(name="CodePostal")
    private String codePostal;

    @Column(name="DateNaissance")
    private Date dateNaissance;

    @Column(name="MotDePasse")
    private String motDePasse;

    @Column(name="PhotoProfil")
    private String photoProfil;

    @Column(name="PermisConduire")
    private String permisConduire;

    @Column(name="CarteIdentite")
    private String carteIdentite;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "UserID"),
            inverseJoinColumns = @JoinColumn(name = "RoleID")
    )
    private List<Role> roles;

}
