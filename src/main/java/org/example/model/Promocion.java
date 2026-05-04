package org.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promociones")
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cliente", length = 100)
    private String nombreCliente;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(name = "copy_ia", nullable = false, columnDefinition = "TEXT")
    private String copyIa;

    @Column(name = "tipo_contenido", length = 20)
    private String tipoContenido; // 'IMAGEN' o 'CARRUSEL'

    @Column(name = "urls_multimedia", nullable = false, columnDefinition = "TEXT")
    private String urlsMultimedia; // URLs separadas por comas

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "telefono_destino")
    private String telefonoDestino;

    @Column(name = "respondido")
    private boolean respondido = false;

    private Boolean estado = true;

    public Promocion() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Promocion(String nombreCliente,String telefonoDestino, String categoria, String copyIa, String tipoContenido, String urlsMultimedia) {
        this.nombreCliente = nombreCliente;
        this.telefonoDestino = telefonoDestino;
        this.categoria = categoria;
        this.copyIa = copyIa;
        this.tipoContenido = tipoContenido;
        this.urlsMultimedia = urlsMultimedia;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = true;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getCopyIa() { return copyIa; }
    public void setCopyIa(String copyIa) { this.copyIa = copyIa; }

    public String getTipoContenido() { return tipoContenido; }
    public void setTipoContenido(String tipoContenido) { this.tipoContenido = tipoContenido; }

    public String getUrlsMultimedia() { return urlsMultimedia; }
    public void setUrlsMultimedia(String urlsMultimedia) { this.urlsMultimedia = urlsMultimedia; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public String getTelefonoDestino() { return telefonoDestino; }
    public void setTelefonoDestino(String telefonoDestino) { this.telefonoDestino = telefonoDestino; }

    public void setRespondido(boolean respondido) {
        this.respondido = respondido;
    }

    public boolean isRespondido() {
        return respondido;
    }
}