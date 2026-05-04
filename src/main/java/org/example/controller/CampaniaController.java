package org.example.controller;

//import jdk.internal.loader.Resource;
import org.example.model.Campania;
import org.example.model.SeguimientoCampania;
import org.example.repository.CampaniaRepository;
import org.example.repository.SeguimientoRepository;
import org.example.service.BotService;
import org.example.service.CampaniaService;
import org.example.service.SeguimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
//import java.net.http.HttpHeaders;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/campanas")
@CrossOrigin(origins = "*")
public class CampaniaController {

    @Autowired
    private CampaniaRepository campaniaRepository;

    @Autowired
    private BotService botService;

    @Autowired
    private SeguimientoService seguimientoService;

    @Autowired
    private SeguimientoRepository seguimientoRepository;

    @Autowired
    private CampaniaService campaniaService;

    @GetMapping("")
    public List<Campania> listarCampanas() {
        List<Campania> lista = campaniaRepository.findAll();
        lista.forEach(c -> {
            if (c.getVisible() == null) {
                c.setVisible(true);
            }
        });
        return lista;
    }
/* bueno
    @PostMapping("/crear")
    public ResponseEntity<Campania> crearCampania(@RequestBody Campania campania) {
        campania.setEstado("PENDIENTE");
        Campania guardada = campaniaRepository.save(campania);

        String previewLanding = botService.generarPreviewLanding(guardada.getNombre());
        guardada.setPreviewLandingUrl(previewLanding);

        campaniaRepository.save(guardada);
        System.out.println("NOTIFICACIÓN PANEL: Nueva campaña pendiente: " + campania.getNombre());

        return ResponseEntity.ok(guardada);
    }*/

    @PostMapping("/crear")
    public ResponseEntity<?> crearCampania(@RequestBody Campania campania) {
        try {
            if (campania.getFechaInicio() == null) {
                campania.setFechaInicio(LocalDate.now());
            }

            campania.setEstado("PENDIENTE");
            Campania guardada = campaniaRepository.save(campania);

            String previewLanding = botService.generarPreviewLanding(guardada.getNombre());
            guardada.setPreviewLandingUrl(previewLanding);

            campaniaRepository.save(guardada);
            System.out.println("NOTIFICACIÓN PANEL/BOT: Nueva campaña pendiente: " + campania.getNombre());

            return ResponseEntity.ok(guardada);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al crear la campaña: " + e.getMessage());
        }
    }
/*
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Campania> aprobarCampania(@PathVariable Long id) {
        System.out.println("➡️ Iniciando aprobación de campaña ID: " + id);
        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));
        String estadoAnterior = campania.getEstado();

        campania.setEstado("APROBADA");
        Campania guardada = campaniaRepository.save(campania);

        seguimientoService.registrar(
                id,
                "APROBADA",
                estadoAnterior,
                "APROBADA",
                "Campaña aprobada correctamente",
                "ADMIN"
        );


        String numeroUsuario = campania.getNumeroUsuario(); // <-- Debes tener este campo en Campania
        String linkLanding = botService.crearLanding(campania.getNombre());

        botService.enviarMensaje(numeroUsuario,
                "✅ Tu campaña \"" + campania.getNombre() + "\" fue aprobada y está activa.\n" +
                        "🌐 Landing generada:\n" + linkLanding
        );
        // Notificación al usuario
        System.out.println("NOTIFICACIÓN USUARIO: Tu campaña fue aprobada: " + campania.getNombre());
        System.out.println("✅ Seguimiento registrado correctamente");


        return ResponseEntity.ok(guardada);
    }*/
    /*
@PostMapping("/{id}/aprobar")
public ResponseEntity<Campania> aprobarCampania(@PathVariable Long id) {
    System.out.println("➡️ Iniciando aprobación de campaña ID: " + id);

    Campania campania = campaniaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));

    campania.setEstado("APROBADA");
    Campania guardada = campaniaRepository.save(campania);

    String linkLanding = botService.crearLanding(guardada.getNombre());
    guardada.setUrlLanding(linkLanding);
    campaniaRepository.save(guardada);
    botService.enviarMensaje(guardada.getNumeroUsuario(),
            "✅ Tu campaña \"" + guardada.getNombre() + "\" fue aprobada y está activa.\n" +
                    "🌐 Landing generada:\n" + linkLanding
    );

    System.out.println("NOTIFICACIÓN USUARIO: Tu campaña fue aprobada: " + guardada.getNombre());
    System.out.println("✅ Seguimiento registrado correctamente");

    return ResponseEntity.ok(guardada);
}*/
@PostMapping("/{id}/aprobar")
public ResponseEntity<Campania> aprobarCampania(@PathVariable Long id) {
    System.out.println("➡️ Iniciando aprobación de campaña ID: " + id);

    Campania campania = campaniaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));

    campania.setEstado("APROBADA");
    Campania guardada = campaniaRepository.save(campania);

    String numeroUsuario = guardada.getNumeroUsuario();
    botService.enviarMensaje(numeroUsuario,
            "✅ Tu campaña \"" + guardada.getNombre() + "\" fue aprobada.\n" +
                    "⏳ La activación y generación de la landing se están procesando. Por favor espera unos momentos."
    );

    System.out.println("NOTIFICACIÓN USUARIO: Campaña aprobada: " + guardada.getNombre());
    System.out.println("✅ Seguimiento registrado correctamente");

    return ResponseEntity.ok(guardada);
}

    @PostMapping("/{id}/activar")
    public ResponseEntity<Campania> activarCampania(@PathVariable Long id) {
        System.out.println("➡️ Activando campaña ID: " + id);
        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));

        if (!"APROBADA".equals(campania.getEstado())) {
            System.out.println("❌ No se puede activar, estado actual: " + campania.getEstado());
            return ResponseEntity.badRequest().body(null);
        }

        String estadoAnterior = campania.getEstado();

        campania.setEstado("ACTIVA");
        Campania guardada = campaniaRepository.save(campania);

        seguimientoService.registrar(
                id,
                "ACTIVADA",
                estadoAnterior,
                "ACTIVA",
                "Campaña activada",
                "ADMIN"
        );

        String numeroUsuario = campania.getNumeroUsuario();
        String linkLanding = botService.crearLanding(campania.getNombre());
        botService.enviarMensaje(numeroUsuario,
                "✅ Tu campaña \"" + campania.getNombre() + "\" fue activada.\n" +
                        "🌐 Landing generada:\n" + linkLanding
        );

        System.out.println("NOTIFICACIÓN USUARIO: Tu campaña está activa: " + campania.getNombre());
        System.out.println("✅ Activación registrada");
        return ResponseEntity.ok(guardada);
    }

    @PostMapping("/{id}/negar")
    public ResponseEntity<Campania> negarCampania(@PathVariable Long id, @RequestParam String motivo) {
        System.out.println("➡️ Negando campaña ID: " + id);
        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));
        String estadoAnterior = campania.getEstado();

        campania.setEstado("NEGADA");
        campania.setMotivoNegacion(motivo);

        Campania guardada = campaniaRepository.save(campania);

        seguimientoService.registrar(
                id,
                "NEGADA",
                estadoAnterior,
                "NEGADA",
                "Motivo: " + motivo,
                "ADMIN"
        );

        String numeroUsuario = campania.getNumeroUsuario();
        botService.enviarMensaje(numeroUsuario,
                "❌ Tu campaña \"" + campania.getNombre() + "\" fue negada.\n" +
                        "Motivo: " + motivo
        );

        System.out.println("NOTIFICACIÓN USUARIO: Tu campaña fue negada. Motivo: " + motivo);
        System.out.println("✅ Negación registrada");
        return ResponseEntity.ok(guardada);
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<?> inactivarCampania(@PathVariable Long id, @RequestParam String motivo) {
        System.out.println("➡️ Inactivando campaña ID: " + id);

        Optional<Campania> cOpt = campaniaRepository.findById(id);
        if (!cOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró la campaña");
        }
        Campania c = cOpt.get();
        String estadoAnterior = c.getEstado();
        c.setEstado("INACTIVA");
        c.setMotivoNegacion(motivo);
        campaniaRepository.save(c);

        seguimientoService.registrar(
                id,
                "INACTIVADA",
                estadoAnterior,
                "INACTIVA",
                "Motivo: " + motivo,
                "ADMIN"
        );
        System.out.println("✅ Inactivación registrada");
        return ResponseEntity.ok(c);
    }
    private void enviarNotificacionPanel(String mensaje) {
    }
    @PostMapping("/{id}/volver-a-valorar")
    public ResponseEntity<?> volverAValorar(@PathVariable Long id) {
        System.out.println("➡️ Volviendo a valorar campaña ID: " + id);

        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));
        String estadoAnterior = campania.getEstado();
        campania.setEstado("PENDIENTE");
        campania.setMotivoNegacion(null); // limpiamos el motivo anterior
        Campania guardada = campaniaRepository.save(campania);
        seguimientoService.registrar(
                id,
                "REVALUADA",
                estadoAnterior,
                "PENDIENTE",
                "Se vuelve a evaluar campaña",
                "ADMIN"
        );
        System.out.println("✅ valoracion registrada");
        return ResponseEntity.ok(guardada);
    }

    @PostMapping("/{id}/ocultar")
    public ResponseEntity<?> ocultarCampania(@PathVariable Long id) {
        System.out.println("➡️ Ocultando campaña ID: " + id);
        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));
        campania.setVisible(false);
        Campania guardada = campaniaRepository.save(campania);
        seguimientoService.registrar(
                id,
                "OCULTADA",
                campania.getEstado(),
                campania.getEstado(),
                "Campaña ocultada del sistema",
                "ADMIN"
        );
        System.out.println("✅ se oculto registrada");
        return ResponseEntity.ok(guardada);
    }

    @PostMapping("/{id}/volver-pendiente")
    public ResponseEntity<Campania> volverAPendiente(@PathVariable Long id) {
        System.out.println("➡️ Volviendo a pendiente campaña ID: " + id);
        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));
        String estadoAnterior = campania.getEstado();
        campania.setEstado("PENDIENTE");
        campania.setMotivoNegacion(null);

        Campania guardada = campaniaRepository.save(campania);

        seguimientoService.registrar(
                id,
                "RESETEADA",
                estadoAnterior,
                "PENDIENTE",
                "Regresó a revisión",
                "ADMIN"
        );
        System.out.println("✅ pendiente registrada");
        return ResponseEntity.ok(guardada);
    }

    @PostMapping("/{id}/editar")
    public ResponseEntity<Campania> editarCampania(@PathVariable Long id, @RequestBody Campania campania) {
        System.out.println("➡️ Editando campaña ID: " + id);

        Campania c = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));

        String cambios =
                "Nombre: " + c.getNombre() + " → " + campania.getNombre();
        c.setNombre(campania.getNombre());
        c.setMensaje(campania.getMensaje());
        c.setDuracionDias(campania.getDuracionDias());
        c.setFechaInicio(campania.getFechaInicio());
        c.setVisible(campania.getVisible());

        Campania guardada = campaniaRepository.save(c);

        seguimientoService.registrar(
                id,
                "EDITADA",
                c.getEstado(),
                c.getEstado(),
                cambios,
                "ADMIN"
        );

        System.out.println("✅ pendiente registrada");
        return ResponseEntity.ok(guardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Campania> actualizarCampania(@PathVariable Long id, @RequestBody Campania campania) {
        Campania c = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));

        // Actualizar campos
        c.setNombre(campania.getNombre());
        c.setMensaje(campania.getMensaje());
        c.setDuracionDias(campania.getDuracionDias());
        c.setFechaInicio(campania.getFechaInicio());
        c.setVisible(campania.getVisible());

        Campania guardada = campaniaRepository.save(c);
        return ResponseEntity.ok(guardada);
    }

    @GetMapping("/{id}/seguimiento")
    public List<SeguimientoCampania> obtenerSeguimiento(@PathVariable Long id) {
        return seguimientoRepository.findByCampaniaIdOrderByFechaAsc(id);
    }

    @GetMapping("/activas")
    public List<Campania> listarCampanasActivas() {
        List<Campania> activas = campaniaRepository.findAll().stream()
                .filter(c -> "ACTIVA".equalsIgnoreCase(c.getEstado()))
                .collect(Collectors.toList());

        for (Campania c : activas) {
            if (c.getVisible() == null) {
                c.setVisible(true);
            }
        }
        return activas;
    }

    @PostMapping("/{id}/regenerar-landing")
    public ResponseEntity<String> regenerarLanding(@PathVariable Long id) {
        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));

        String previewLink = botService.generarPreviewLanding(campania.getNombre());
        campania.setPreviewLandingUrl(previewLink);
        campaniaRepository.save(campania);

        return ResponseEntity.ok(previewLink);
    }

    @PostMapping("/{id}/preview-landing")
    public ResponseEntity<String> obtenerPreviewLanding(@PathVariable Long id) {
        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));

        String previewLink = campania.getPreviewLandingUrl();

        if (previewLink == null || previewLink.contains("❌")) {

            previewLink = botService.generarPreviewLanding(campania.getNombre());
            campania.setPreviewLandingUrl(previewLink);
            campaniaRepository.save(campania);
        }

        return ResponseEntity.ok(previewLink);
    }

}