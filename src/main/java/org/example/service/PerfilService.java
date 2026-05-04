package org.example.service;

import org.example.model.Perfil;
import org.example.repository.PerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfilService {

    @Autowired
    private PerfilRepository perfilRepository;

    public List<Perfil> listarPerfiles() {
        return perfilRepository.findAll();
    }

    public Perfil crearPerfil(Perfil perfil) {
        return perfilRepository.save(perfil);
    }

    public Perfil obtenerPorNombre(String nombre) {
        return perfilRepository.findByNombre(nombre);
    }

    public void eliminarPerfil(Long id) {
        perfilRepository.deleteById(id);
    }
}