package org.example.dto;

import java.util.List;

public class EnvioMasivoDTO {
    private String campaniaId;
    private String nombreEmpresa;
    private String mensajeTexto;
    private String landingUrl;
    private List<String> destinatarios;


    public EnvioMasivoDTO() {}

    public String getCampaniaId() { return campaniaId; }
    public void setCampaniaId(String campaniaId) { this.campaniaId = campaniaId; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getMensajeTexto() { return mensajeTexto; }
    public void setMensajeTexto(String mensajeTexto) { this.mensajeTexto = mensajeTexto; }

    public String getLandingUrl() { return landingUrl; }
    public void setLandingUrl(String landingUrl) { this.landingUrl = landingUrl; }

    public List<String> getDestinatarios() { return destinatarios; }
    public void setDestinatarios(List<String> destinatarios) { this.destinatarios = destinatarios; }


}
