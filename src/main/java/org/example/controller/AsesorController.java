package org.example.controller;

import org.example.model.Asesor;
import org.example.repository.AsesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/asesores")
@CrossOrigin(origins = "*")
public class AsesorController {

    @Autowired
    private AsesorRepository asesorRepository;

    @GetMapping
    public List<Asesor> listarTodos() {
        return asesorRepository.findAll();
    }

    @PostMapping
    public Asesor guardar(@RequestBody Asesor asesor) {
        return asesorRepository.save(asesor);
    }
/* ESTE ES EL CORRECTO
    @GetMapping("/atender")
    public ResponseEntity<Void> registrarGanador(@RequestParam("data") String data) {
        try {
            String[] partes = data.split("&asesor=");
            String telCliente = partes[0];
            String nombreAsesor = partes[1];

            asesorRepository.findByNombreIgnoreCase(nombreAsesor).ifPresent(a -> {
                asesorRepository.findAll().forEach(as -> as.setEsGanador(false));

                a.setEsGanador(true);
                a.setLeadsAtendidos(a.getLeadsAtendidos() + 1);
                a.setUltimoTiempoRespuesta("15s");
                asesorRepository.save(a);
            });

            String urlWhatsapp = "https://wa.me/" + telCliente;
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(urlWhatsapp))
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }*/

    @GetMapping("/atender")
    public ResponseEntity<Void> registrarGanador(@RequestParam("data") String data) {
        try {
            String[] partes = data.split("_ID_");
            String telCliente = partes[0];
            String nombreAsesor = partes[1].replace("_", " ");

            asesorRepository.findByNombreIgnoreCase(nombreAsesor).ifPresent(a -> {
                List<Asesor> todos = asesorRepository.findAll();
                todos.forEach(as -> as.setEsGanador(false));
                asesorRepository.saveAll(todos);

                a.setEsGanador(true);
                a.setLeadsAtendidos(a.getLeadsAtendidos() + 1);
                a.setUltimoTiempoRespuesta(java.time.LocalTime.now().toString().substring(0, 5));
                asesorRepository.save(a);

                System.out.println("🏆 Botón clickeado por: " + a.getNombre() + " para lead: " + telCliente);
            });

            String urlWhatsapp = "https://wa.me/" + telCliente;
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(urlWhatsapp))
                    .build();

        } catch (Exception e) {
            System.err.println("❌ Error en /atender: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
