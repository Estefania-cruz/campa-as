package org.example.service;

import antlr.Token;
import org.example.model.Asesor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.repository.CampaniaRepository;
import org.example.model.Campania;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Optional;

import java.util.Map;
@Service
public class BotService {

    @Autowired
    private CampaniaRepository campaniaRepository;
    private final String TOKEN = "EAALcxf2qvM8BRL4u8SpQPr8bRTm1gwBfk0MXC84DnhmMt3W1hCmUnfdzJZB5zVLA7UrR9ZAHD9R3xG1BXqzBZBwuTh0kN6PLasQRXCrKh1VoZCLtDbZC8E7RSdslZBnI1yqKIsgXMplGKmMBlnWiD7qH6WLAvPcjLa1tVmPibg67lDXqhMc7EXqVisc2DgITTAvQZDZD";
    private final String PHONE_NUMBER_ID = "1004648086076413";
    @Autowired
    private  IAService iaService;
    public String responderHola(){

        int hora = LocalTime.now().getHour();
        String saludo;

        if (hora >= 6 && hora < 12) {
            saludo = "Buenos días";
        } else if (hora >= 12 && hora < 20) {
            saludo = "Buenas tardes";
        } else {
            saludo = "Buenas noches";
        }

        return "Hola soy Efecampañas\n\n" +
                saludo +
                "\n\nSelecciona una opción:\n\n" +
                "1️⃣ Crear campaña\n" +
                "2️⃣ Activar campaña\n" +
                "3️⃣ Desactivar campaña\n" +
                "4️⃣ Ver campañas activas\n" +
                "5️⃣ Hacer landing de campaña\n";
    }

    public String activarCampania(String nombreCampania){

        Optional<Campania> campaniaOpt = campaniaRepository.findAll()
                .stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombreCampania))
                .findFirst();

        if(campaniaOpt.isPresent()){

            Campania campania = campaniaOpt.get();

            if("ACTIVA".equalsIgnoreCase(campania.getEstado())){
                return "La campaña '" + nombreCampania + "' ya se encuentra activa.";
            }

            campania.setEstado("ACTIVA");
            campaniaRepository.save(campania);

            return "✅ Campaña '" + nombreCampania + "' reactivada correctamente.";
        }

        return "❌ No se encontró la campaña '" + nombreCampania + "'.";
    }


    public void enviarMensaje(String numero, String mensaje) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = "https://graph.facebook.com/v18.0/1004648086076413/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(TOKEN);

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", numero);
            body.put("type", "text");

            Map<String, String> text = new HashMap<>();
            text.put("body", mensaje);

            body.put("text", text);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            System.out.println("✅ Enviado a Meta: " + response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String desactivarCampania(String nombreCampania){

        Optional<Campania> campaniaOpt = campaniaRepository.findAll()
                .stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombreCampania))
                .findFirst();

        if(campaniaOpt.isPresent()){

            Campania campania = campaniaOpt.get();

            if("INACTIVA".equalsIgnoreCase(campania.getEstado())){
                return "La campaña '" + nombreCampania + "' ya está desactivada.";
            }

            campania.setEstado("INACTIVA");
            campaniaRepository.save(campania);

            return "❌ Campaña '" + nombreCampania + "' desactivada correctamente.";
        }

        return "No se encontró la campaña '" + nombreCampania + "'.";
    }
    public String crearLanding(String nombreCampania){

        Optional<Campania> campaniaOpt = campaniaRepository.findAll()
                .stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombreCampania)
                        && ("ACTIVA".equalsIgnoreCase(c.getEstado()) || "APROBADA".equalsIgnoreCase(c.getEstado())))
                .findFirst();

        if(!campaniaOpt.isPresent()){
            return "❌ No se encontró una campaña activa con ese nombre.";
        }
        Campania campania = campaniaOpt.get();
        String nombreArchivo = campania.getNombre()
                .replace(" ", "_")
                .toLowerCase();
        String ruta = "landings/" + nombreArchivo + ".html";
        java.io.File file = new java.io.File(ruta);

        if(file.exists()){

            //return "http://localhost:8082/landings/" + nombreArchivo + ".html";
            return "https://gaffe-skeptic-hangnail.ngrok-free.dev/landings/" + nombreArchivo + ".html";
        }

        String html = iaService.generarLanding(campania);

        try{

            file.getParentFile().mkdirs();

            java.nio.file.Files.write(
                    java.nio.file.Paths.get(ruta),
                    html.getBytes()
            );

        }catch(Exception e){

            e.printStackTrace();
            if(html.contains("Error generando landing")){
                return "❌ No se pudo generar la landing con IA. Intenta nuevamente.";
            }
        }
        System.out.println("Archivo guardado en: " + ruta);
        System.out.println(file.getAbsolutePath());
        //return "http://localhost:8082/landings/" + nombreArchivo + ".html";
        return "https://gaffe-skeptic-hangnail.ngrok-free.dev/landings/" + nombreArchivo + ".html";
    }

    public String generarPreviewLanding(String nombreCampania) {

        Optional<Campania> campaniaOpt = campaniaRepository.findAll()
                .stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombreCampania))
                .findFirst();

        if (!campaniaOpt.isPresent()) {
            return "❌ No se encontró la campaña para previsualización.";
        }

        Campania campania = campaniaOpt.get();
        String nombreArchivo = campania.getNombre()
                .trim()
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_")
                .toLowerCase() + "_preview";

        String ruta = "previews/" + nombreArchivo + ".html";
        java.io.File file = new java.io.File(ruta);

        if (file.exists()) {
            return "https://gaffe-skeptic-hangnail.ngrok-free.dev/previews/" + nombreArchivo + ".html";
        }

        String html = iaService.generarLanding(campania);

        try {
            file.getParentFile().mkdirs();
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(ruta),
                    html.getBytes()
            );
        } catch (Exception e) {
            e.printStackTrace();
            if (html.contains("Error generando landing")) {
                return "❌ No se pudo generar la landing de preview. Intenta nuevamente.";
            }
        }

        System.out.println("Preview guardado en: " + ruta);
        return "https://gaffe-skeptic-hangnail.ngrok-free.dev/previews/" + nombreArchivo + ".html";
    }





}