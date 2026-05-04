package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.repository.UsuarioRepository;
import org.example.model.Usuario;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public boolean login(String correo, String password){

        Usuario u = usuarioRepository.findByCorreo(correo);

        if (u == null) {
            return false;
        }

        return u.getPassword().equals(password);
    }
}