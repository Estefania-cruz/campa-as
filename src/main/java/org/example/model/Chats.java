package org.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "chats")
public class Chats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telefono_usuario", nullable = false)
    private String telefonoUsuario;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private String remitente;

    @Column(name = "mensaje_id_wa")
    private String mensajeIdWa;

    @Column(name = "estado_wa")
    private String estadoWa;

    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio = OffsetDateTime.now();

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(columnDefinition = "TEXT")
    private String ultimoError;

    @Column(name = "respondido")
    private Boolean respondido = false;
    private LocalDateTime fecha;
    public Chats() {
    }

    public Chats(String telefonoUsuario,String nombreUsuario, String mensaje, String remitente) {
        this.telefonoUsuario = telefonoUsuario;
        this.nombreUsuario = nombreUsuario;
        this.mensaje = mensaje;
        this.remitente = remitente;
        this.fechaEnvio = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTelefonoUsuario() {
        return telefonoUsuario;
    }

    public void setTelefonoUsuario(String telefonoUsuario) {
        this.telefonoUsuario = telefonoUsuario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public String getMensajeIdWa() {
        return mensajeIdWa;
    }

    public void setMensajeIdWa(String mensajeIdWa) {
        this.mensajeIdWa = mensajeIdWa;
    }

    public String getEstadoWa() {
        return estadoWa;
    }

    public void setEstadoWa(String estadoWa) {
        this.estadoWa = estadoWa;
    }

    public OffsetDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(OffsetDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public boolean isRespondido() {
        return respondido;
    }

    public Boolean getRespondido() {
        return respondido;
    }

    public void setRespondido(Boolean respondido) {
        this.respondido = respondido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String ultimoError) { this.ultimoError = ultimoError; }

}
