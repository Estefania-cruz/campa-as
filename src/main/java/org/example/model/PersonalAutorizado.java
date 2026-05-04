package org.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "personal_autorizado")
public class PersonalAutorizado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String puesto;
    private String numero;
    private String correo;

    private String tokenVerificacion;
    private LocalDateTime tokenGeneradoAt;
    @Column(columnDefinition = "int default 0")
    private int intentosFallidos = 0;

    @Column(columnDefinition = "boolean default false")
    private boolean bloqueado = false;

    public PersonalAutorizado() {
    }

    public PersonalAutorizado(String nombre, String puesto, String numero, String correo) {
        this.nombre = nombre;
        this.puesto = puesto;
        this.numero = numero;
        this.correo = correo;
    }


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

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTokenVerificacion() {
        return tokenVerificacion;
    }

    public void setTokenVerificacion(String tokenVerificacion) {
        this.tokenVerificacion = tokenVerificacion;
    }

    public LocalDateTime getTokenGeneradoAt() {
        return tokenGeneradoAt;
    }

    public void setTokenGeneradoAt(LocalDateTime tokenGeneradoAt) {
        this.tokenGeneradoAt = tokenGeneradoAt;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

}
