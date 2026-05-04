package org.example.model;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String correo;
    private String password;
    private String rol;
    private String username;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Permisos permisos;

    // Constructor vacío (OBLIGATORIO)
    public Usuario() {
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Permisos getPermisos() { return permisos; }
    public void setPermisos(Permisos permisos) {
        this.permisos = permisos;
        if(permisos != null) permisos.setUsuario(this);
    }

}