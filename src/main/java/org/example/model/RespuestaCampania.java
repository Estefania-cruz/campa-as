package org.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "respuestas_campania")
public class RespuestaCampania {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telefono_usuario")
    private String telefonoUsuario;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(name = "respuesta")
    private String respuesta;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta = LocalDateTime.now();

    public RespuestaCampania() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTelefonoUsuario() { return telefonoUsuario; }
    public void setTelefonoUsuario(String telefonoUsuario) { this.telefonoUsuario = telefonoUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }
}