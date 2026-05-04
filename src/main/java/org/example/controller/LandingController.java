package org.example.controller;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class LandingController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @GetMapping("/landings/{archivo}")
    public ResponseEntity<Resource> verLanding(@PathVariable String archivo) throws Exception {

        Path path = Paths.get("landings/" + archivo);

        if(!path.toFile().exists()){
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/previews/{archivo}")
    public ResponseEntity<Resource> verPreview(@PathVariable String archivo) throws Exception {

        //Path path = Paths.get("previews/" + archivo);
        //Path path = Paths.get("/Users/aiengine/IdeaProjects/efecampana/previews/" + archivo);
        Path path = Paths.get(System.getProperty("user.dir"), "previews", archivo);
        System.out.println("Buscando archivo en: " + path.toAbsolutePath());
        if (!path.toFile().exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @PostMapping("/api/landing/contacto")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> guardarContacto(@RequestParam Map<String, String> params) {

        String nombre = params.get("nombre");
        String empresa = params.get("empresa");
        String telefono = params.get("telefono");
        String correo = params.get("correo");
        String comentarios = params.get("comentarios");

        String sql = "INSERT INTO formulario_landing (nombre, empresa, telefono, correo, comentarios) VALUES (?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql, nombre, empresa, telefono, correo, comentarios);
            return ResponseEntity.ok().body("{\"status\": \"success\"}"); // 🚩 Respondemos JSON
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }


}