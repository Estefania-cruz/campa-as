package org.example.controller;

import org.example.model.PersonalAutorizado;
import org.example.service.PersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal")
@CrossOrigin(origins = "*")
public class PersonalController {

    @Autowired
    private PersonalService service;

    @GetMapping("/lista")
    public List<PersonalAutorizado> listarTodos() {
        return service.obtenerTodos();
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregar(@RequestBody PersonalAutorizado p) {
        service.guardar(p);
        return ResponseEntity.ok("{\"res\": \"Agregado con éxito\"}");
    }

    @PostMapping("/desbloquear")
    public ResponseEntity<?> desbloquear(@RequestParam String numero) {
        PersonalAutorizado p = service.buscarPorNumero(numero);
        p.setBloqueado(false);
        p.setIntentosFallidos(0);
        service.guardar(p);
        return ResponseEntity.ok("{\"res\": \"Usuario desbloqueado\"}");
    }

    @PostMapping("/reenviar-token")
    public ResponseEntity<?> reenviar(@RequestParam String numero) {
        return ResponseEntity.ok("{\"res\": \"Token nuevo enviado\"}");
    }
}