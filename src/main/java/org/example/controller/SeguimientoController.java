package org.example.controller;

import org.example.model.SeguimientoCampania;
import org.example.repository.SeguimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seguimiento")
@CrossOrigin(origins = "*")
public class SeguimientoController {

    @Autowired
    private SeguimientoRepository repo;

    @GetMapping("/{campaniaId}")
    public List<SeguimientoCampania> obtenerSeguimiento(@PathVariable Long campaniaId) {
        return repo.findByCampaniaIdOrderByFechaAsc(campaniaId);
    }
}