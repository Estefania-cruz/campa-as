package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "asesores")
public class Asesor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(name = "apellido_p")
    private String apellidoP;

    @Column(name = "apellido_m")
    private String apellidoM;

    private String grupo;
    private String telefono;

    @Enumerated(EnumType.STRING)
    private EstatusAsesor estatus = EstatusAsesor.LIBRE;

    @Column(name = "leads_atendidos")
    private int leadsAtendidos = 0;

    @Column(name = "ultimo_tiempo_respuesta")
    private String ultimoTiempoRespuesta;

    @Column(name = "es_ganador")
    private boolean esGanador = false;

    private String ultimoClienteAtendido;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoP() { return apellidoP; }
    public void setApellidoP(String apellidoP) { this.apellidoP = apellidoP; }

    public String getApellidoM() { return apellidoM; }
    public void setApellidoM(String apellidoM) { this.apellidoM = apellidoM; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public EstatusAsesor getEstatus() { return estatus; }
    public void setEstatus(EstatusAsesor estatus) { this.estatus = estatus; }

    public int getLeadsAtendidos() { return leadsAtendidos; }
    public void setLeadsAtendidos(int leadsAtendidos) { this.leadsAtendidos = leadsAtendidos; }

    public String getUltimoTiempoRespuesta() { return ultimoTiempoRespuesta; }
    public void setUltimoTiempoRespuesta(String ultimoTiempoRespuesta) { this.ultimoTiempoRespuesta = ultimoTiempoRespuesta; }

    public boolean isEsGanador() { return esGanador; }
    public void setEsGanador(boolean esGanador) { this.esGanador = esGanador; }

    public String getUltimoClienteAtendido() { return ultimoClienteAtendido; }
    public void setUltimoClienteAtendido(String ultimoClienteAtendido) { this.ultimoClienteAtendido = ultimoClienteAtendido; }

public enum EstatusAsesor {
    LIBRE,
    EN_LLAMADA,
    BLOQUEADO
}
}