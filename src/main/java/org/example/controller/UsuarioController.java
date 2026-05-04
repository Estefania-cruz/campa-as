
package org.example.controller;

import org.example.model.Permisos;
import org.example.model.Usuario;
import org.example.repository.PermisosRepository;
import org.example.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PermisosRepository permisosRepository;

    /*@GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }*/

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAllWithPermisos();
    }

    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {

        usuario.setCreatedAt(LocalDateTime.now());
        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        Permisos p = new Permisos();
        p.setUsuario(nuevoUsuario);
        nuevoUsuario.setPermisos(p);
        switch(usuario.getRol()) {
            case "admin":
                p.setMenuDashboard(true);
                p.setMenuCampanas(true);
                p.setMenuCrear(true);
                p.setMenuSeguimientos(true);
                p.setMenuConversaciones(true);
                p.setMenuEjecutivos(true);
                p.setMenuInteresados(true);
                p.setMenuReportes(true);
                p.setMenuPerfiles(true);
                p.setMenuPersonal(true);
                p.setMenuEnvio(true);
                p.setMenuSeguiEnvio(true);
                p.setMenuPromociones(true);
                break;
            case "supervisor":
                p.setMenuDashboard(true);
                p.setMenuCampanas(true);
                p.setMenuCrear(true);
                p.setMenuSeguimientos(true);
                p.setMenuConversaciones(true);
                p.setMenuInteresados(true);
                p.setMenuReportes(true);
                p.setMenuPerfiles(false);
                p.setMenuEnvio(true);
                p.setMenuSeguiEnvio(true);
                p.setMenuPromociones(true);
                break;
            case "agente":
                p.setMenuDashboard(true);
                p.setMenuSeguimientos(true);
                p.setMenuInteresados(true);
                p.setMenuReportes(true);
                p.setMenuPerfiles(false);
                break;
        }
        permisosRepository.save(p);
        nuevoUsuario.setPermisos(p);
        return nuevoUsuario;
    }

}