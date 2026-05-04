package org.example.controller;


import org.example.service.CampaniaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

    @Autowired
    private CampaniaService campaniaService;

    @PostMapping("/aprobar/{id}/{numero}")
    public ResponseEntity<String> aprobar(@PathVariable Long id, @PathVariable String numero) {
        campaniaService.aprobarCampania(id, numero);
        return ResponseEntity.ok("Campaña aprobada y activada ✅");
    }
}