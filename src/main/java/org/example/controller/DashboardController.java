package org.example.controller;

import org.example.model.Campania;
import org.example.model.Chats;
import org.example.repository.CampaniaRepository;
import org.example.repository.ChatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.service.DashboardService;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private CampaniaRepository campaniaRepository;

    @GetMapping("/dashboard")
   /* public Map<String, Object> dashboard(){
        return dashboardService.obtenerDatosDashboard();
    }*/
    public Map<String, Object> dashboard(@RequestParam(defaultValue = "0") int page) {
        return dashboardService.obtenerDatosDashboard(page);
    }

    @GetMapping("/campanas-dashboard")
    public List<Campania> listarCampanas() {
        return campaniaRepository.findAll();
    }

    @GetMapping("/estadisticas/{telefono}")
    public ResponseEntity<Map<String, Long>> getEstadisticasPromocion(@PathVariable String telefono) {

        String telLimpio = telefono.replace("+", "").trim();

        List<Chats> listaChats = chatRepository.findByTelefonoUsuarioOrderByFechaEnvioAsc(telLimpio);

        Map<String, Long> stats = listaChats.stream()
                .filter(c -> c.getEstadoWa() != null)
                .collect(Collectors.groupingBy(Chats::getEstadoWa, Collectors.counting()));

        long respuestas = listaChats.stream()
                .filter(c -> c.getRespondido() != null && c.getRespondido())
                .count();

        stats.put("interacciones", respuestas);

        return ResponseEntity.ok(stats);
    }



}