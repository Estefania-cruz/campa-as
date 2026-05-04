package org.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs_envios_whatsapp")
public class EnvioLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campania_id")
    private String campaniaId;

    @Column(name = "nombre_empresa")
    private String nombreEmpresa;

    @Column(name = "mensaje_texto", columnDefinition = "TEXT")
    private String mensajeTexto;

    @Column(name = "url_landing")
    private String urlLanding;

    @Column(name = "total_destinatarios")
    private Integer totalDestinatarios;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "telefono_destino")
    private String telefonoDestino;


    @Column(name = "mensaje_id_wa")
    private String mensajeIdWa;

    @Column(name = "estado_wa")
    private String estadoWa;

    @Column(name = "respondido")
    private boolean respondido = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCampaniaId() { return campaniaId; }
    public void setCampaniaId(String campaniaId) { this.campaniaId = campaniaId; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getMensajeTexto() { return mensajeTexto; }
    public void setMensajeTexto(String mensajeTexto) { this.mensajeTexto = mensajeTexto; }

    public String getUrlLanding() { return urlLanding; }
    public void setUrlLanding(String urlLanding) { this.urlLanding = urlLanding; }

    public Integer getTotalDestinatarios() { return totalDestinatarios; }
    public void setTotalDestinatarios(Integer totalDestinatarios) { this.totalDestinatarios = totalDestinatarios; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getTelefonoDestino() { return telefonoDestino; }
    public void setTelefonoDestino(String telefonoDestino) { this.telefonoDestino = telefonoDestino; }

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

    public boolean isRespondido() {
        return respondido;
    }

    public void setRespondido(boolean respondido) {
        this.respondido = respondido;
    }
}