package org.example.controller;

import org.example.model.Chats;
import org.example.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatRepository chatRepository;

    @GetMapping("/conversaciones")
    public Map<String, List<Chats>> obtenerConversaciones() {
        List<Chats> todos = chatRepository.findAll();
        return todos.stream()
                .collect(Collectors.groupingBy(Chats::getTelefonoUsuario));
    }

    @GetMapping("/historial/{telefono}")
    public List<Chats> obtenerHistorial(@PathVariable String telefono) {
        return chatRepository.findByTelefonoUsuarioOrderByFechaEnvioAsc(telefono);
    }

/* sin cambio de moneda
    @GetMapping("/rendimiento")
    public ResponseEntity<Map<String, Object>> getRendimiento() {
        Map<String, Object> data = new HashMap<>();

        long totalEnviados = chatRepository.count();
        long totalLeidos = chatRepository.countByEstadoWa("read");
        long totalRespuestas = chatRepository.countByRespondido(true);

        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        long enviadosHoy = chatRepository.countByFechaAfter(inicioHoy);
        long leidosHoy = chatRepository.countByEstadoWaAndFechaAfter("read", inicioHoy);
        long respuestasHoy = chatRepository.countByRespondidoAndFechaAfter(true, inicioHoy);

        data.put("totalHistorico", totalEnviados);
        data.put("enviados", enviadosHoy);
        data.put("leidos", leidosHoy);
        data.put("respuestas", respuestasHoy);

        double porcentaje = (enviadosHoy > 0) ? ((double) leidosHoy / enviadosHoy) * 100 : 0;
        data.put("tasaLectura", Math.round(porcentaje));

        return ResponseEntity.ok(data);
    }
    */

   // @CrossOrigin(origins = "http://localhost:4200")
   /* @GetMapping("/rendimiento")
    public ResponseEntity<Map<String, Object>> getRendimiento() {
        Map<String, Object> data = new HashMap<>();
        RestTemplate restTemplate = new RestTemplate();

        double tipoCambio = 11.50;
        try {
            String urlApi = "https://api.exchangerate-api.com/v4/latest/AUD";
            Map<String, Object> response = restTemplate.getForObject(urlApi, Map.class);
            if (response != null && response.containsKey("rates")) {
                Map<String, Object> rates = (Map<String, Object>) response.get("rates");
                tipoCambio = Double.parseDouble(rates.get("MXN").toString());
            }
        } catch (Exception e) {
            System.err.println("No se pudo obtener el tipo de cambio: " + e.getMessage());
        }
        long totalEnviados = chatRepository.count();
        long totalLeidos = chatRepository.countByEstadoWa("read");
        long totalRespuestas = chatRepository.countByRespondido(true);

        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        long enviadosHoy = chatRepository.countByFechaAfter(inicioHoy);
        long leidosHoy = chatRepository.countByEstadoWaAndFechaAfter("read", inicioHoy);
        long respuestasHoy = chatRepository.countByRespondidoAndFechaAfter(true, inicioHoy);
        long totalHistorico = chatRepository.count();
        double costoMensajeAUD = 0.50;
        double inversionTotalMXN = totalEnviados * costoMensajeAUD * tipoCambio;
        double inversionHoyMXN = enviadosHoy * costoMensajeAUD * tipoCambio;

        double tasaLecturaHoy = (enviadosHoy > 0) ? ((double) leidosHoy / enviadosHoy) * 100 : 0;

        data.put("enviados", enviadosHoy);
        data.put("leidos", leidosHoy);
        data.put("respuestas", respuestasHoy);
        data.put("tasaLectura", Math.round(tasaLecturaHoy));
        data.put("inversionHoyMXN", Math.round(inversionHoyMXN * 100.0) / 100.0);

        data.put("totalHistorico", totalEnviados);
        data.put("totalLeidosHistorico", totalLeidos);
        data.put("inversionTotalMXN", Math.round(inversionTotalMXN * 100.0) / 100.0);

        data.put("tipoCambioAUDMXN", tipoCambio);

        return ResponseEntity.ok(data);
    }*/
   @GetMapping("/rendimiento")
   public ResponseEntity<Map<String, Object>> getRendimiento() {
       Map<String, Object> data = new HashMap<>();
       RestTemplate restTemplate = new RestTemplate();

       double tipoCambioActual = 12.42;
       try {
           String url = "https://api.exchangerate-api.com/v4/latest/AUD";
           Map<String, Object> response = restTemplate.getForObject(url, Map.class);
           if (response != null && response.containsKey("rates")) {
               Map<String, Object> rates = (Map<String, Object>) response.get("rates");
               tipoCambioActual = Double.parseDouble(rates.get("MXN").toString());
           }
       } catch (Exception e) {
           System.err.println("Error consultando API de moneda, usando 12.42");
       }
       long totalMensajes = chatRepository.count();

       double costoMensajeAUD = 0.01105;

       double inversionTotalMXN = totalMensajes * costoMensajeAUD * tipoCambioActual;

       data.put("totalHistorico", totalMensajes);
       data.put("tipoCambioAUDMXN", tipoCambioActual);
       data.put("inversionTotalMXN", Math.round(inversionTotalMXN * 100.0) / 100.0);

       LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
       long enviadosHoy = chatRepository.countByFechaAfter(inicioHoy);
       double inversionHoyMXN = enviadosHoy * costoMensajeAUD * tipoCambioActual;

       data.put("enviados", enviadosHoy);
       data.put("inversionHoyMXN", Math.round(inversionHoyMXN * 100.0) / 100.0);

       return ResponseEntity.ok(data);
   }

    @GetMapping("/rendimiento-diario")
    public ResponseEntity<List<Map<String, Object>>> getRendimientoDiario() {
        List<Object[]> resultados = chatRepository.countMessagesByDay();
        List<Map<String, Object>> historial = new ArrayList<>();

        double costoMensajeAUD = 0.01105;
        double tipoCambioMXN = 12.42;

        for (Object[] fila : resultados) {
            Map<String, Object> punto = new HashMap<>();

            // fila[0] es la fecha que viene de la base de datos
            punto.put("fecha", fila[0].toString());

            // fila[1] es el conteo (COUNT)
            long entregados = ((Number) fila[1]).longValue();
            punto.put("entregados", entregados);

            // Cálculo de dinero
            double cargosDiaMXN = entregados * costoMensajeAUD * tipoCambioMXN;
            punto.put("cargos", Math.round(cargosDiaMXN * 100.0) / 100.0);

            historial.add(punto);
        }

        return ResponseEntity.ok(historial);
    }



}