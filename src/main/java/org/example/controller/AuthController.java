package org.example.controller;

import org.example.model.Usuario;
import org.example.repository.UsuarioRepository;
import org.example.service.AuthService;
import org.example.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
/*
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        boolean valido = authService.login(request.getCorreo(), request.getPassword());
        Map<String, Object> res = new HashMap<>();

        if (valido) {
            res.put("success", true);
            res.put("message", "Login correcto");
            return ResponseEntity.ok(res);
        } else {
            res.put("success", false);
            res.put("message", "Credenciales incorrectas");
            return ResponseEntity.status(401).body(res);
        }
    }*/

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        boolean valido = authService.login(request.getCorreo(), request.getPassword());
        Map<String, Object> res = new HashMap<>();

        if (valido) {

            Usuario usuario = usuarioRepository.findByCorreoWithPermisos(request.getCorreo());

            res.put("success", true);
            res.put("message", "Login correcto");
            res.put("usuario", usuario);

            return ResponseEntity.ok(res);

        } else {
            res.put("success", false);
            res.put("message", "Credenciales incorrectas");
            return ResponseEntity.status(401).body(res);
        }
    }


}