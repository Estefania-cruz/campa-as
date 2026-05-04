package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.dto.EnvioMasivoDTO;
import org.example.model.Asesor;
import org.example.model.EnvioLog;
import org.example.repository.AsesorRepository;
import org.example.repository.ChatRepository;
import org.example.repository.EnvioLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {


    @Autowired
    private EnvioLogRepository envioLogRepository;

@Autowired
private ChatRepository chatRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

   private final String TOKEN = "EAALcxf2qvM8BRL4u8SpQPr8bRTm1gwBfk0MXC84DnhmMt3W1hCmUnfdzJZB5zVLA7UrR9ZAHD9R3xG1BXqzBZBwuTh0kN6PLasQRXCrKh1VoZCLtDbZC8E7RSdslZBnI1yqKIsgXMplGKmMBlnWiD7qH6WLAvPcjLa1tVmPibg67lDXqhMc7EXqVisc2DgITTAvQZDZD";
   private final String PHONE_NUMBER_ID = "1004648086076413";
  //  private final String PHONE_NUMBER_ID = "1062610620266583";


/*
   public void enviarMensaje(String numero, String mensaje) {
       //coloque eso
       if (mensaje == null || mensaje.trim().isEmpty()) {
           System.err.println("⚠️ Intento de enviar mensaje vacío a: " + numero);
           return;
       }
//coloque eso
        try {

            String url = "https://graph.facebook.com/v18.0/" + PHONE_NUMBER_ID + "/messages";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(TOKEN);
            headers.setContentType(MediaType.APPLICATION_JSON);

            //coloque eso
            String mensajeLimpio = mensaje.replaceAll("[\\t\\n\\r]", " ").trim();

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            //coloque eso abajo
            body.put("recipient_type", "individual");
            //
            body.put("to", numero);
            body.put("type", "text");

            Map<String, String> text = new HashMap<>();
            text.put("body", mensajeLimpio);
           // text.put("body", mensaje);
            body.put("text", text);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ Mensaje enviado con éxito a: " + numero);
            System.out.println("Respuesta API WhatsApp:");
            System.out.println(response.getBody());

        } catch (Exception e) {
            System.out.println("Error enviando mensaje a WhatsApp:");
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                String errorResponse = ((org.springframework.web.client.HttpClientErrorException) e).getResponseBodyAsString();
                System.err.println("Cuerpo del error: " + errorResponse);
            }
            e.printStackTrace();
        }
    }

*/
public void enviarMensaje(String numero, String mensaje) {
    if (mensaje == null || mensaje.trim().isEmpty()) {
        System.err.println("⚠️ Intento de enviar mensaje vacío a: " + numero);
        return;
    }

    try {
        String url = "https://graph.facebook.com/v18.0/" + PHONE_NUMBER_ID + "/messages";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String mensajeFinal = mensaje.trim();

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", numero);
        body.put("type", "text");

        Map<String, String> text = new HashMap<>();
        text.put("body", mensajeFinal);
        body.put("text", text);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        System.out.println("✅ Mensaje enviado con éxito a: " + numero);
        System.out.println("Respuesta API WhatsApp: " + response.getBody());

    } catch (Exception e) {
        System.out.println("❌ Error enviando mensaje a WhatsApp:");
        String statusMeta = "failed";
        String errorMeta = e.getMessage();
        if (e instanceof org.springframework.web.client.HttpClientErrorException) {
            String errorResponse = ((org.springframework.web.client.HttpClientErrorException) e).getResponseBodyAsString();
            System.err.println("Cuerpo del error Meta API: " + errorResponse);
        }

        String finalError = errorMeta;
        chatRepository.findByTelefonoUsuario(numero).ifPresent(chat -> {
            chat.setEstadoWa("failed");
            chat.setUltimoError(finalError);
            chatRepository.save(chat);
        });

        e.printStackTrace();
    }
}

    private final String accessToken = "EAALcxf2qvM8BRL4u8SpQPr8bRTm1gwBfk0MXC84DnhmMt3W1hCmUnfdzJZB5zVLA7UrR9ZAHD9R3xG1BXqzBZBwuTh0kN6PLasQRXCrKh1VoZCLtDbZC8E7RSdslZBnI1yqKIsgXMplGKmMBlnWiD7qH6WLAvPcjLa1tVmPibg67lDXqhMc7EXqVisc2DgITTAvQZDZD";
    public byte[] descargarMedia(String mediaId) {
        try {
            String urlMetadata = "https://graph.facebook.com/v21.0/" + mediaId;
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(this.accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            JsonNode response = restTemplate.exchange(urlMetadata, HttpMethod.GET, entity, JsonNode.class).getBody();

            String downloadUrl = response.path("url").asText();

            return restTemplate.exchange(downloadUrl, HttpMethod.GET, entity, byte[].class).getBody();
        } catch (Exception e) {
            System.err.println("Error descargando audio: " + e.getMessage());
            return null;
        }
    }


    public void enviarCampania(List<String> numeros, String mensaje){
        for(String numero : numeros){
            enviarMensaje(numero, mensaje);
        }
    }

    private final String URL_META = "https://graph.facebook.com/v18.0/" + PHONE_NUMBER_ID + "/messages";
/*
    @Async
    public void procesarEnvioMasivo(EnvioMasivoDTO datos) {


        EnvioLog nuevoLog = new EnvioLog();
        nuevoLog.setCampaniaId(datos.getCampaniaId());
        nuevoLog.setNombreEmpresa(datos.getNombreEmpresa());
        nuevoLog.setMensajeTexto(datos.getMensajeTexto());
        nuevoLog.setUrlLanding(datos.getLandingUrl());
        nuevoLog.setTotalDestinatarios(datos.getDestinatarios().size());
        nuevoLog.setFechaEnvio(LocalDateTime.now());

        try {
            envioLogRepository.save(nuevoLog);
            System.out.println("✅ Registro guardado en BD para la empresa: " + datos.getNombreEmpresa());
        } catch (Exception e) {
            System.err.println("❌ No se pudo guardar el log: " + e.getMessage());
        }

        RestTemplate restTemplate = new RestTemplate();

        for (String numero : datos.getDestinatarios()) {
            try {
                String numeroDestino = numero.trim().replaceAll("[^0-9]", "");

                String cuerpoMensaje = "¡Hola! * " + datos.getNombreEmpresa() + "* informa:\n\n" +
                        datos.getMensajeTexto() + "\n\n" +
                        "👉 Accede aquí: " + datos.getLandingUrl();

                Map<String, Object> body = new HashMap<>();
                body.put("messaging_product", "whatsapp");
                body.put("recipient_type", "individual");
                body.put("to", numeroDestino);
                body.put("type", "text");

                Map<String, String> text = new HashMap<>();
                text.put("body", cuerpoMensaje);
                text.put("preview_url", "true");
                body.put("text", text);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(TOKEN);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                restTemplate.postForEntity(URL_META, request, String.class);

                Thread.sleep(500);

            } catch (Exception e) {
                System.err.println("Error enviando a " + numero + ": " + e.getMessage());
            }
        }
    }*/
/*
@Async
public void procesarEnvioMasivo(EnvioMasivoDTO datos) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(TOKEN);

    for (String numero : datos.getDestinatarios()) {
        try {
            String numeroDestino = numero.trim().replaceAll("[^0-9]", "");

            if (numeroDestino.length() == 10) numeroDestino = "52" + numeroDestino;

            EnvioLog logIndividual = new EnvioLog();
            logIndividual.setCampaniaId(datos.getCampaniaId());
            logIndividual.setNombreEmpresa(datos.getNombreEmpresa());
            logIndividual.setMensajeTexto(datos.getMensajeTexto());
            logIndividual.setUrlLanding(datos.getLandingUrl());
            logIndividual.setTelefonoDestino(numeroDestino);
            logIndividual.setTotalDestinatarios(datos.getDestinatarios().size());
            logIndividual.setFechaEnvio(LocalDateTime.now());

            envioLogRepository.save(logIndividual);

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("recipient_type", "individual");
            body.put("to", numeroDestino);
            body.put("type", "interactive");


            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "button");

            Map<String, String> bodyContent = new HashMap<>();
            bodyContent.put("text", "¡Hola! *" + datos.getNombreEmpresa() + "* informa:\n\n" +
                    datos.getMensajeTexto() + "\n\n" +
                    "👉 Landing: " + datos.getLandingUrl());
            interactive.put("body", bodyContent);

            Map<String, Object> action = new HashMap<>();
            List<Map<String, Object>> buttons = new ArrayList<>();
            buttons.add(crearBoton("btn_interes", "Me interesa ✅"));
            buttons.add(crearBoton("btn_no_gracias", "No, gracias ❌"));

            action.put("buttons", buttons);
            interactive.put("action", action);
            body.put("interactive", interactive);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(URL_META, request, String.class);

            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("Error enviando a " + numero + ": " + e.getMessage());
        }
    }
}*/
@Async
public void procesarEnvioMasivo(EnvioMasivoDTO datos) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(TOKEN);

    for (String numero : datos.getDestinatarios()) {
        try {
            String numeroDestino = numero.trim().replaceAll("[^0-9]", "");
            if (numeroDestino.length() == 10) numeroDestino = "52" + numeroDestino;

            EnvioLog logIndividual = new EnvioLog();
            logIndividual.setCampaniaId(datos.getCampaniaId());
            logIndividual.setNombreEmpresa(datos.getNombreEmpresa());
            logIndividual.setMensajeTexto(datos.getMensajeTexto());
            logIndividual.setUrlLanding(datos.getLandingUrl());
            logIndividual.setTelefonoDestino(numeroDestino);
            logIndividual.setTotalDestinatarios(datos.getDestinatarios().size());
            logIndividual.setFechaEnvio(LocalDateTime.now());
            envioLogRepository.save(logIndividual);

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", numeroDestino);
            body.put("type", "template");

            Map<String, Object> template = new HashMap<>();
            template.put("name", "campana_seguimiento_dinamica");

            Map<String, String> language = new HashMap<>();
            language.put("code", "es");
            template.put("language", language);

            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> bodyComponent = new HashMap<>();
            bodyComponent.put("type", "body");

            List<Map<String, Object>> parameters = new ArrayList<>();

            parameters.add(crearParametroTexto(datos.getNombreEmpresa()));
            parameters.add(crearParametroTexto(datos.getMensajeTexto()));
            parameters.add(crearParametroTexto(datos.getLandingUrl()));

            bodyComponent.put("parameters", parameters);
            components.add(bodyComponent);
            template.put("components", components);
            body.put("template", template);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(URL_META, request, String.class);

            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("❌ Error enviando a " + numero + ": " + e.getMessage());
        }
    }
}

    private Map<String, Object> crearParametroTexto(String texto) {
        Map<String, Object> param = new HashMap<>();
        param.put("type", "text");
        param.put("text", texto != null ? texto : "");
        return param;
    }
    private Map<String, Object> crearBoton(String id, String titulo) {
        Map<String, Object> button = new HashMap<>();
        button.put("type", "reply");
        Map<String, String> reply = new HashMap<>();
        reply.put("id", id);
        reply.put("title", titulo);
        button.put("reply", reply);
        return button;
    }

    public List<Map<String, Object>> obtenerReporteSeguimiento() {
        String sql = "SELECT " +
                "  c.id AS campania_id, " +
                "  c.nombre AS campania, " +
                "  COALESCE(l.nombre_empresa, c.nombre) AS empresa, " +
                "  COALESCE(l.telefono_destino, r.telefono_usuario) AS telefono, " +
                "  COALESCE(l.fecha_envio, r.fecha_respuesta) AS fecha, " +
                "  COALESCE(r.nombre_usuario, 'Cliente Potencial') AS nombre, " +
                "  COALESCE(r.respuesta, 'SIN_RESPUESTA') AS estatus, " +
                "  COALESCE(l.url_landing, c.url_landing) AS url " +
                "FROM campanas c " +
                "LEFT JOIN logs_envios_whatsapp l ON l.campania_id = CAST(c.id AS VARCHAR) " +
                "FULL OUTER JOIN respuestas_campania r ON RIGHT(l.telefono_destino, 10) = RIGHT(r.telefono_usuario, 10) " +
                "ORDER BY " +
                "  CASE " +
                "    WHEN r.respuesta = 'INTERESADO' THEN 1 " +
                "    WHEN r.respuesta = 'NO_INTERESADO' THEN 2 " +
                "    ELSE 3 " +
                "  END, fecha DESC";

        return jdbcTemplate.queryForList(sql);
    }

    public void enviarCarruselAWhatsApp(String telefono, String urlImagen, String textoIA) {
        String urlMeta = "https://graph.facebook.com/v21.0/" + PHONE_NUMBER_ID + "/messages";

        String jsonPayload = "{"
                + "\"messaging_product\": \"whatsapp\","
                + "\"recipient_type\": \"individual\","
                + "\"to\": \"" + telefono + "\","
                + "\"type\": \"template\","
                + "\"template\": {"
                + "  \"name\": \"promociones_ia_carrusel\","
                + "  \"language\": { \"code\": \"es_MX\" },"
                + "  \"components\": ["
                + "    {"
                + "      \"type\": \"body\","
                + "      \"parameters\": ["
                + "        { \"type\": \"text\", \"text\": \"Fanyny\" }," // Variable {{1}} (Nombre)
                + "        { \"type\": \"text\", \"text\": \"despensa\" }" // Variable {{2}} (Producto)
                + "      ]"
                + "    },"
                + "    {"
                + "      \"type\": \"carousel\","
                + "      \"cards\": ["
                + "        {"
                + "          \"card_index\": 0,"
                + "          \"components\": ["
                + "            { \"type\": \"header\", \"parameters\": [ { \"type\": \"image\", \"image\": { \"link\": \"" + urlImagen + "\" } } ] },"
                + "            { \"type\": \"body\", \"parameters\": [ { \"type\": \"text\", \"text\": \"" + textoIA + "\" } ] },"
                + "            { \"type\": \"button\", \"sub_type\": \"quick_reply\", \"index\": 0, \"parameters\": [ { \"type\": \"text\", \"text\": \"Más información\" } ] }"
                + "          ]"
                + "        }"
                + "      ]"
                + "    }"
                + "  ]"
                + "}"
                + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(urlMeta, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ Mensaje carrusel enviado con éxito a: " + telefono);
                System.out.println("Respuesta de Meta: " + response.getBody());
            } else {
                System.err.println("⚠️ Error al enviar: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Error crítico en la conexión con Meta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PostMapping("/enviar-promocion")
    public ResponseEntity<String> enviar(@RequestBody Map<String, String> request) {
        String telefono = request.get("telefono");
        String urlImagen = request.get("imagenUrl");
        String copy = request.get("copy");

        enviarCarruselAWhatsApp(telefono, urlImagen, copy);

        return ResponseEntity.ok("Campaña disparada");
    }
}