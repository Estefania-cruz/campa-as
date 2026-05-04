
package org.example.model;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "permisos")
public class Permisos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    private boolean menuDashboard;
    private boolean menuCampanas;
    private boolean menuSeguimientos;
    private boolean menuEjecutivos;
    private boolean menuInteresados;
    private boolean menuReportes;
    private boolean menuPerfiles;
    private boolean menuCrear;
    private boolean menuConversaciones;
    private boolean menuPersonal;
    private boolean menuEnvio;
    @Column(name = "menu_segui_envio", nullable = false, columnDefinition = "boolean default false")
    private boolean menuSeguiEnvio = false;

    private boolean menuPromociones = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean isMenuDashboard() {
        return menuDashboard;
    }

    public void setMenuDashboard(boolean menuDashboard) {
        this.menuDashboard = menuDashboard;
    }

    public boolean isMenuCampanas() {
        return menuCampanas;
    }

    public void setMenuCampanas(boolean menuCampanas) {
        this.menuCampanas = menuCampanas;
    }

    public boolean isMenuSeguimientos() {
        return menuSeguimientos;
    }

    public void setMenuSeguimientos(boolean menuSeguimientos) {
        this.menuSeguimientos = menuSeguimientos;
    }

    public boolean isMenuEjecutivos() {
        return menuEjecutivos;
    }

    public void setMenuEjecutivos(boolean menuEjecutivos) {
        this.menuEjecutivos = menuEjecutivos;
    }

    public boolean isMenuInteresados() {
        return menuInteresados;
    }

    public void setMenuInteresados(boolean menuInteresados) {
        this.menuInteresados = menuInteresados;
    }

    public boolean isMenuReportes() {
        return menuReportes;
    }

    public void setMenuReportes(boolean menuReportes) {
        this.menuReportes = menuReportes;
    }

    public boolean isMenuPerfiles() {
        return menuPerfiles;
    }

    public void setMenuPerfiles(boolean menuPerfiles) {
        this.menuPerfiles = menuPerfiles;
    }

    public boolean isMenuCrear() {
        return menuCrear;
    }

    public void setMenuCrear(boolean menuCrear) {
        this.menuCrear = menuCrear;
    }

    public boolean isMenuConversaciones() {
        return menuConversaciones;
    }

    public void setMenuConversaciones(boolean menuConversaciones) {
        this.menuConversaciones = menuConversaciones;
    }

    public boolean isMenuPersonal() {
        return menuPersonal;
    }

    public void setMenuPersonal(boolean menuPersonal) {
        this.menuPersonal = menuPersonal;
    }


    public boolean isMenuEnvio() {
        return menuEnvio;
    }

    public void setMenuEnvio(boolean menuEnvio) {
        this.menuEnvio = menuEnvio;
    }

    public boolean isMenuSeguiEnvio() {
        return menuSeguiEnvio;
    }

    public void setMenuSeguiEnvio(boolean menuSeguiEnvio) {
        this.menuSeguiEnvio = menuSeguiEnvio;
    }

    public boolean isMenuPromociones() {
        return menuPromociones;
    }

    public void setMenuPromociones(boolean menuPromociones) {
        this.menuPromociones = menuPromociones;
    }


}