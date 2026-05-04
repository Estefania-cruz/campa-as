package org.example.controller;

import org.example.model.Perfil;
import org.example.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {

    @Autowired
    private PerfilService perfilService;

    @GetMapping
    public List<Perfil> listarPerfiles() {
        return perfilService.listarPerfiles();
    }

    @PostMapping
    public Perfil crearPerfil(@RequestBody Perfil perfil) {
        return perfilService.crearPerfil(perfil);
    }

    @GetMapping("/{nombre}")
    public Perfil obtenerPerfil(@PathVariable String nombre) {
        return perfilService.obtenerPorNombre(nombre);
    }

    @DeleteMapping("/{id}")
    public void eliminarPerfil(@PathVariable Long id) {
        perfilService.eliminarPerfil(id);
    }
}