package org.example.model;

import javax.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name = "campanas")
public class Campania {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String estado;
    private int duracionDias;
    private String imagen;
    private String mensaje;
    private Integer efectividad;
    @Column(name = "fecha_inicio", nullable = true)
    private LocalDate fechaInicio;
    private String tipo;
    private String asesorTelefono;
    private String motivoNegacion;
    private Boolean visible;
    private String numeroUsuario;
    @Column(name = "url_landing")
    private String urlLanding;
    @Column(name = "url_reporte")
    private String urlReporte;
    private String previewLandingUrl;


    public Long getId() {
        return id;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getDuracionDias() { return duracionDias; }
    public void setDuracionDias(int duracionDias) { this.duracionDias = duracionDias; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Integer getEfectividad() { return efectividad; }
    public void setEfectividad(Integer efectividad) { this.efectividad = efectividad; }
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getAsesorTelefono() {
        return asesorTelefono;
    }

    public void setAsesorTelefono(String asesorTelefono) {
        this.asesorTelefono = asesorTelefono;
    }

    public String getMotivoNegacion() {
        return motivoNegacion;
    }

    public void setMotivoNegacion(String motivoNegacion) {
        this.motivoNegacion = motivoNegacion;
    }

    public boolean isVisible() {
        return visible;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getNumeroUsuario() { return numeroUsuario; }
    public void setNumeroUsuario(String numeroUsuario) { this.numeroUsuario = numeroUsuario; }

    public String getUrlLanding() {
        return urlLanding;
    }

    public void setUrlLanding(String urlLanding) {
        this.urlLanding = urlLanding;
    }

    public String getUrlReporte() {
        return urlReporte;
    }

    public void setUrlReporte(String urlReporte) {
        this.urlReporte = urlReporte;
    }

    public String getPreviewLandingUrl() {
        return previewLandingUrl;
    }
    public void setPreviewLandingUrl(String previewLandingUrl) {
        this.previewLandingUrl = previewLandingUrl;
    }


}