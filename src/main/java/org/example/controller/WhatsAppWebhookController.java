package org.example.controller;

import antlr.Token;
import org.example.model.*;
import org.example.repository.*;
import org.example.service.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.controller.WhatsAppController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/webhook")
public class WhatsAppWebhookController {

    @Autowired
    private BotService botService;

    @Autowired
    @Lazy
    private WhatsAppController whatsAppController;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private RespuestaCampaniaRepository respuestaRepository;

    @Autowired
    private EnvioLogRepository envioLogRepository;

    @Autowired
    private AsesorRepository asesorRepository;

    @Autowired
private PromocionRepository promocionRepository;

    @Autowired
    private EmailService emailService;
    private final Map<String, String> estadoUsuario = new ConcurrentHashMap<>();

    private final String TOKEN = "EAALcxf2qvM8BRL4u8SpQPr8bRTm1gwBfk0MXC84DnhmMt3W1hCmUnfdzJZB5zVLA7UrR9ZAHD9R3xG1BXqzBZBwuTh0kN6PLasQRXCrKh1VoZCLtDbZC8E7RSdslZBnI1yqKIsgXMplGKmMBlnWiD7qH6WLAvPcjLa1tVmPibg67lDXqhMc7EXqVisc2DgITTAvQZDZD";

    private static final String VERIFY_TOKEN = "efecampanas_token";


    @GetMapping
    public String verificarWebhook(
            @RequestParam(name = "hub.mode") String mode,
            @RequestParam(name = "hub.challenge") String challenge,
            @RequestParam(name = "hub.verify_token") String token) {

        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            return challenge;
        }

        return "Error de verificación";
    }
/*
    @PostMapping
    public String recibirMensaje(@RequestBody String payload) {
        System.out.println("📩 PAYLOAD COMPLETO:");
        System.out.println(payload);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);
            JsonNode entry = root.path("entry").get(0);
            if(entry == null) return "EVENT_RECEIVED";
            JsonNode value = entry.path("changes").get(0).path("value");
            if(value.has("messages")) {
                JsonNode messageNode = value.path("messages").get(0);
                String numero = messageNode.path("from").asText();
                String waId = messageNode.path("id").asText();
                String tipoMensaje = messageNode.path("type").asText();
                String nombreUsuario = "Usuario WhatsApp";
                if(value.has("contacts")) {
                    nombreUsuario = value.path("contacts").get(0).path("profile").path("name").asText("Usuario WhatsApp");
                }
                String mensajeOriginal = "";
                if("text".equals(tipoMensaje)) {
                    mensajeOriginal = messageNode.path("text").path("body").asText();
                }
                // ============================================================
                // 1. PRIORIDAD: ¿ES UN ASESOR GANANDO UN LEAD?
                // ============================================================
                String mensajeMinusculas = mensajeOriginal.toLowerCase().trim();
                Optional < Asesor > asesorEncontrado = asesorRepository.findAll().stream().filter(a -> numero.equals(a.getTelefono()) || numero.endsWith(a.getTelefono())).findFirst();
                if(asesorEncontrado.isPresent() && (mensajeMinusculas.contains("atiendo") || mensajeMinusculas.contains("atender"))) {
                    Asesor a = asesorEncontrado.get();
                    // Lógica de Ganador
                    List < Asesor > todos = asesorRepository.findAll();
                    todos.forEach(as-> as.setEsGanador(false));
                    asesorRepository.saveAll(todos);
                    a.setEsGanador(true);
                    a.setLeadsAtendidos(a.getLeadsAtendidos() + 1);
                    a.setUltimoTiempoRespuesta(java.time.LocalTime.now().withNano(0).toString());
                    asesorRepository.save(a);
                    // LOGS EN CONSOLA
                    System.out.println("==============================================");
                    System.out.println("🏆 ASESOR IDENTIFICADO: " + a.getNombre() + " " + (a.getApellidoP() != null ? a.getApellidoP() : ""));
                    System.out.println("✅ ACCIÓN: Lead asignado correctamente en PostgreSQL");
                    System.out.println("==============================================");
                    whatsAppService.enviarMensaje(numero, "🏆 ¡Confirmado " + a.getNombre() + "! El lead se ha registrado a tu nombre. ¡A darle!");
                    return "EVENT_RECEIVED";
                }
                // ============================================================
                // 2. LOGICA DE BOTONES (INTERÉS DEL CLIENTE)
                // ============================================================
                String respuestaParaCliente = "";
                String mensajeParaGuardarLog = mensajeOriginal;
                if("button".equals(tipoMensaje)) {
                    String buttonText = messageNode.path("button").path("text").asText();
                    mensajeParaGuardarLog = "[Botón: " + buttonText + "]";
                    if(buttonText.contains("Me interesa")) {
                        // Obtener datos de campaña
                        EnvioLog ultimoEnvio = envioLogRepository.findFirstByTelefonoDestinoOrderByFechaEnvioDesc(numero);
                        String campania = (ultimoEnvio != null) ? ultimoEnvio.getCampaniaId() : "Campaña General";
                        String landing = (ultimoEnvio != null) ? ultimoEnvio.getUrlLanding() : "https://efectivale.com.mx";

                        List < Asesor > asesores = asesorRepository.findAll();
                        for(Asesor as: asesores) {
                            if(as.getLeadsAtendidos() < 10) {
                                enviarPlantillaAsesor(as.getTelefono(), as.getNombre(), nombreUsuario, numero, campania, landing);
                            }
                        }
                        respuestaParaCliente = "¡Excelente! Ya avisamos a nuestro equipo. Un asesor te contactará de inmediato. ✨";
                    } else {
                        respuestaParaCliente = "Entendido. Si cambias de opinión, aquí estaremos. ¡Buen día! 🙂";
                    }
                }
                // ============================================================
                // 3. LÓGICA DE CLIENTES O PERSONAL (RESTO DEL FLUJO)
                // ============================================================
                else if("text".equals(tipoMensaje)) {
                    respuestaParaCliente = whatsAppController.procesarMensaje(numero, mensajeOriginal);
                } else if("audio".equals(tipoMensaje)) {
                    String mediaId = messageNode.path("audio").path("id").asText();
                    respuestaParaCliente = whatsAppController.procesarAudio(numero, mediaId);
                    mensajeParaGuardarLog = "[Audio Transcrito]";
                }
                // ============================================================
                // 4. GUARDADO DE CHATS E HISTORIAL
                // ============================================================
                Chats msgUser = new Chats();
                msgUser.setTelefonoUsuario(numero);
                msgUser.setNombreUsuario(nombreUsuario);
                msgUser.setMensaje(mensajeParaGuardarLog);
                msgUser.setRemitente("user");
                msgUser.setMensajeIdWa(waId);
                msgUser.setFechaEnvio(java.time.OffsetDateTime.now());
                chatRepository.save(msgUser);
                System.out.println(msgUser);
                if(respuestaParaCliente != null && !respuestaParaCliente.isEmpty()) {
                    whatsAppService.enviarMensaje(numero, respuestaParaCliente);
                    Chats msgBot = new Chats();
                    msgBot.setTelefonoUsuario(numero);
                    msgBot.setNombreUsuario("Efecampañas");
                    msgBot.setMensaje(respuestaParaCliente);
                    msgBot.setRemitente("bot");
                    msgBot.setFechaEnvio(java.time.OffsetDateTime.now());
                    chatRepository.save(msgBot);
                    System.out.println(msgBot);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error en Webhook: " + e.getMessage());
            e.printStackTrace();
        }
        return "EVENT_RECEIVED";
    }
*/
    /*
    private void enviarPlantillaAsesor(String telefonoAsesor, String nombreAsesor, String nombreCliente, String telCliente, String campania, String landing) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + TOKEN);
            Map < String, Object > body = new HashMap < > ();
            body.put("messaging_product", "whatsapp");
            body.put("to", telefonoAsesor);
            body.put("type", "template");
            Map < String, Object > template = new HashMap < > ();
            template.put("name", "notificacion_asesor");
            Map < String, String > language = new HashMap < > ();
            language.put("code", "en");
            template.put("language", language);
            List < Map < String, Object >> components = new ArrayList < > ();
            // --- COMPONENTE BODY ({{1}}, {{2}}, {{3}}, {{4}}) ---
            Map < String, Object > bodyComponent = new HashMap < > ();
            bodyComponent.put("type", "body");
            List < Map < String, Object >> bodyParams = new ArrayList < > ();
            bodyParams.add(crearParam(nombreCliente)); // {{1}}
            bodyParams.add(crearParam(telCliente)); // {{2}}
            bodyParams.add(crearParam(campania)); // {{3}}
            bodyParams.add(crearParam(landing)); // {{4}}
            bodyComponent.put("parameters", bodyParams);
            components.add(bodyComponent);
            Map < String, Object > buttonComponent = new HashMap < > ();
            buttonComponent.put("type", "button");
            buttonComponent.put("sub_type", "url");
            buttonComponent.put("index", "0");
            List < Map < String, Object >> buttonParams = new ArrayList < > ();
            Map < String, Object > pBtn = new HashMap < > ();
            pBtn.put("type", "text");
            String numeroLimpio = telCliente.replaceAll("[^0-9]", "").trim();
            String parametroBoton = numeroLimpio + "&asesor=" + nombreAsesor;
            pBtn.put("text", parametroBoton);
            buttonParams.add(pBtn);
            buttonComponent.put("parameters", buttonParams);
            components.add(buttonComponent);
            template.put("components", components);
            body.put("template", template);
            HttpEntity < Map < String, Object >> request = new HttpEntity < > (body, headers);
            String urlMeta = "https://graph.facebook.com/v21.0/1004648086076413/messages";
            ResponseEntity < String > response = restTemplate.postForEntity(urlMeta, request, String.class);
            System.out.println("----------------------------------------------");
            System.out.println("📢 ALERTA ENVIADA A: " + nombreAsesor);
            System.out.println("📦 Campaña: " + campania);
            System.out.println("🔗 Cliente: " + nombreCliente);
            System.out.println("Status API: " + response.getStatusCode());
            System.out.println("----------------------------------------------");
        } catch (Exception e) {
            System.err.println("❌ Error enviando plantilla al asesor (" + nombreAsesor + "): " + e.getMessage());
        }
    }
 */
    /*SIN LOGICA HUMANA OSEA SIN PREVE HUMANA JAJAJA DIFICIL DE ENTENDER XD
@PostMapping
public String recibirMensaje(@RequestBody String payload) {
    System.out.println("📩 PAYLOAD COMPLETO:");
    System.out.println(payload);
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(payload);
        JsonNode entry = root.path("entry").get(0);
        if (entry == null) return "EVENT_RECEIVED";
        JsonNode value = entry.path("changes").get(0).path("value");

        if (value.has("messages")) {
            JsonNode messageNode = value.path("messages").get(0);
            String numeroRemitente = messageNode.path("from").asText();
            String waId = messageNode.path("id").asText();
            String tipoMensaje = messageNode.path("type").asText();

            String nombreUsuario = "Usuario";
            if (value.has("contacts")) {
                nombreUsuario = value.path("contacts").get(0).path("profile").path("name").asText("Usuario");
            }
            nombreUsuario = nombreUsuario.replaceAll("[\\n\\r\\t]", " ").replaceAll("\\s{2,}", " ").trim();

            String mensajeOriginal = "text".equals(tipoMensaje) ? messageNode.path("text").path("body").asText() : "";

            String msgMin = mensajeOriginal.toLowerCase().trim();
            Optional<Asesor> asesorOpt = asesorRepository.findAll().stream()
                    .filter(a -> numeroRemitente.equals(a.getTelefono()) || numeroRemitente.endsWith(a.getTelefono()))
                    .findFirst();

            if (asesorOpt.isPresent() && (msgMin.contains("atiendo") || msgMin.contains("atender")|| msgMin.contains("atendere")|| msgMin.contains("ayudo")|| msgMin.contains("apoyar"))) {
                Asesor asesorActual = asesorOpt.get();

                Chats ultimoAviso = chatRepository.findFirstByTelefonoUsuarioAndMensajeContainingOrderByFechaEnvioDesc(
                        asesorActual.getTelefono(), "Cliente");

                if (ultimoAviso != null) {
                    String telCliente = ultimoAviso.getMensaje().replaceAll("[^0-9]", "");

                    Optional<Asesor> yaGanadoPor = asesorRepository.findAll().stream()
                            .filter(as -> telCliente.equals(as.getUltimoClienteAtendido()))
                            .findFirst();

                    if (yaGanadoPor.isPresent()) {
                        Asesor ganador = yaGanadoPor.get();
                        if (!ganador.getTelefono().equals(asesorActual.getTelefono())) {

                            String respuestaRebote = "Lo sentimos mucho, pero el lead con el numero  " + telCliente +
                                    " ya está siendo atendido por el asesor " + ganador.getNombre() + ". Gracias por tu atención. Ten una buena jornada";

                            whatsAppService.enviarMensaje(numeroRemitente, respuestaRebote);
                            return "EVENT_RECEIVED";
                        }
                    }

                    List<Asesor> todos = asesorRepository.findAll();
                    todos.forEach(as -> as.setEsGanador(false));
                    asesorRepository.saveAll(todos);

                    asesorActual.setEsGanador(true);
                    asesorActual.setLeadsAtendidos(asesorActual.getLeadsAtendidos() + 1);
                    asesorActual.setUltimoClienteAtendido(telCliente);
                    asesorActual.setUltimoTiempoRespuesta(java.time.LocalTime.now().toString().substring(0, 5));
                    asesorRepository.save(asesorActual);

                    System.out.println("🏆 ASESOR: " + asesorActual.getNombre() + " GANÓ AL CLIENTE: " + telCliente);

                    whatsAppService.enviarMensaje(numeroRemitente, "🏆 ¡Confirmado " + asesorActual.getNombre() + "! El lead se ha registrado a tu nombre.");

                    String mensajeAlCliente = "¡Hola! 👋 Te tenemos una excelente noticia: el asesor *" + asesorActual.getNombre() +
                            "* ha sido asignado para apoyarte personalmente. En un par de minutos se pondrá en contacto contigo por este medio. ✨";

                    whatsAppService.enviarMensaje(telCliente, mensajeAlCliente);
                    guardarHistorial(telCliente, "SISTEMA", "[Asignado a: " + asesorActual.getNombre() + "]", null, "bot");
                } else {
                    whatsAppService.enviarMensaje(numeroRemitente, "No encontré un lead pendiente en tu historial.");
                }
                return "EVENT_RECEIVED";
            }

            String respuestaBot = "";
            String logMsj = mensajeOriginal;

            if ("button".equals(tipoMensaje)) {
                String buttonText = messageNode.path("button").path("text").asText();
                logMsj = "[Botón: " + buttonText + "]";

                if (buttonText.contains("Me interesa")) {

                    EnvioLog ultimoEnvio = envioLogRepository.findFirstByTelefonoDestinoOrderByFechaEnvioDesc(numeroRemitente);

                    String camp = (ultimoEnvio != null) ? ultimoEnvio.getCampaniaId() : "Campaña General";

                    String land = (ultimoEnvio != null) ? ultimoEnvio.getUrlLanding() : "https://efectivale.com.mx";

                    List<Asesor> asesores = asesorRepository.findAll();

                    for (Asesor as : asesores) {

                        if (as.getLeadsAtendidos() < 10) {

                            enviarPlantillaAsesor(as.getTelefono(), as.getNombre(), nombreUsuario, numeroRemitente, camp, land);

                        }

                    }
                    respuestaBot = "¡Excelente! Ya avisamos a nuestro equipo. Un asesor te contactará de inmediato. ✨";
                }
            } else if ("text".equals(tipoMensaje)) {
                respuestaBot = whatsAppController.procesarMensaje(numeroRemitente, mensajeOriginal);
            } else if ("audio".equals(tipoMensaje)) {
                respuestaBot = whatsAppController.procesarAudio(numeroRemitente, messageNode.path("audio").path("id").asText());
                logMsj = "[Audio Transcrito]";
            }

            guardarHistorial(numeroRemitente, nombreUsuario, logMsj, waId, "user");
            if (respuestaBot != null && !respuestaBot.isEmpty()) {
                whatsAppService.enviarMensaje(numeroRemitente, respuestaBot);
                guardarHistorial(numeroRemitente, "Efecampañas", respuestaBot, null, "bot");
            }
        }
    } catch (Exception e) {
        System.err.println("❌ Error: " + e.getMessage());
    }
    return "EVENT_RECEIVED";
}

private void enviarPlantillaAsesor(String telefonoAsesor, String nombreAsesor, String nombreCliente, String telCliente, String campania, String landing) {
    try {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + TOKEN);

        String nomCli = nombreCliente.replaceAll("[\\n\\r\\t]", " ").replaceAll("\\s{2,}", " ").trim();
        String camp = campania.replaceAll("[\\n\\r\\t]", " ").replaceAll("\\s{2,}", " ").trim();
        String land = landing.replaceAll("\\s", "");
        String telLimpio = telCliente.replaceAll("[^0-9]", "");

        String asesorSinEspacios = nombreAsesor.replace(" ", "_");

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", telefonoAsesor);
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", "notificacion_asesor");
        template.put("language", Collections.singletonMap("code", "en"));

        List<Map<String, Object>> components = new ArrayList<>();

        // --- 2. COMPONENTE BODY ({{1}}, {{2}}, {{3}}, {{4}}) ---
        Map<String, Object> bodyComp = new HashMap<>();
        bodyComp.put("type", "body");
        bodyComp.put("parameters", Arrays.asList(
                crearParam(nomCli),
                crearParam(telLimpio),
                crearParam(camp),
                crearParam(land)
        ));
        components.add(bodyComp);

        Map<String, Object> btnComp = new HashMap<>();
        btnComp.put("type", "button");
        btnComp.put("sub_type", "url");
        btnComp.put("index", "0");

        String valBtn = telLimpio + "_ID_" + asesorSinEspacios;

        btnComp.put("parameters", Collections.singletonList(crearParam(valBtn)));
        components.add(btnComp);

        template.put("components", components);
        body.put("template", template);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String urlMeta = "https://graph.facebook.com/v21.0/1004648086076413/messages";

        ResponseEntity<String> response = restTemplate.postForEntity(urlMeta, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String msjParaHistorial = "Cliente: " + nomCli + " Tel:" + telLimpio;
            guardarHistorial(telefonoAsesor, "Sistema", msjParaHistorial, null, "bot");

            System.out.println("----------------------------------------------");
            System.out.println("📢 ALERTA ENVIADA Y REGISTRADA PARA: " + nombreAsesor);
            System.out.println("📦 Lead: " + nomCli + " (" + telLimpio + ")");
            System.out.println("----------------------------------------------");
        }

    } catch (Exception e) {
        System.err.println("❌ Error enviando plantilla: " + e.getMessage());
    }
}
*/

/*
    @PostMapping
    public String recibirMensaje(@RequestBody String payload) {
        System.out.println("📩 PAYLOAD COMPLETO:");
        System.out.println(payload);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);
            JsonNode entry = root.path("entry").get(0);
            if (entry == null) return "EVENT_RECEIVED";
            JsonNode value = entry.path("changes").get(0).path("value");

            if (value.has("messages")) {
                JsonNode messageNode = value.path("messages").get(0);
                String numeroRemitente = messageNode.path("from").asText();
                String waId = messageNode.path("id").asText();
                String tipoMensaje = messageNode.path("type").asText();

                String mensajeOriginal = "text".equals(tipoMensaje) ? messageNode.path("text").path("body").asText() : "";
                String msgMin = mensajeOriginal.toLowerCase().trim();

                Optional<Asesor> asesorOpt = asesorRepository.findAll().stream()
                        .filter(a -> numeroRemitente.equals(a.getTelefono()) || numeroRemitente.endsWith(a.getTelefono()))
                        .findFirst();

                if (asesorOpt.isPresent()) {
                    Asesor asesorActual = asesorOpt.get();

                    if (msgMin.contains("atiendo") || msgMin.contains("atender")) {
                        return procesarAsesorGanaLead(asesorActual, numeroRemitente);
                    }

                    String telClienteAsignado = asesorActual.getUltimoClienteAtendido();
                    if (telClienteAsignado != null) {
                        if (msgMin.contains("finalizar chat")) {
                            return finalizarChatHumano(asesorActual, telClienteAsignado, numeroRemitente);
                        }

                        if (!mensajeOriginal.isEmpty()) {
                            whatsAppService.enviarMensaje(telClienteAsignado, mensajeOriginal);
                            guardarHistorial(telClienteAsignado, asesorActual.getNombre(), mensajeOriginal, waId, "bot");
                        }
                        return "EVENT_RECEIVED";
                    }
                }

                String estadoC = estadoUsuario.get(numeroRemitente);
                if (estadoC != null && estadoC.startsWith("MODO_HUMANO_")) {
                    String telAsesorAsignado = estadoC.replace("MODO_HUMANO_", "");

                    if (!mensajeOriginal.isEmpty()) {
                        String feedbackParaAsesor = "👤 *Cliente:* " + mensajeOriginal;
                        whatsAppService.enviarMensaje(telAsesorAsignado, feedbackParaAsesor);
                        guardarHistorial(numeroRemitente, "Usuario", mensajeOriginal, waId, "user");
                    }
                    return "EVENT_RECEIVED";
                }

                return procesarLogicaBot(value, messageNode, numeroRemitente, tipoMensaje, mensajeOriginal, waId);
            }
        } catch (Exception e) {
            System.err.println("❌ Error en recibir Mensaje: " + e.getMessage());
        }
        return "EVENT_RECEIVED";
    }*/
/* jala dia 20 lunbes
    @PostMapping
    public String recibirMensaje(@RequestBody String payload) {
        System.out.println("\n--- [Step 1] PAYLOAD RECIBIDO ---");
        System.out.println(payload);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);
            JsonNode entry = root.path("entry").get(0);
            if (entry == null) {
                System.out.println("⚠️ Entry es nulo");
                return "EVENT_RECEIVED";
            }

            JsonNode value = entry.path("changes").get(0).path("value");

            if (value.has("messages")) {
                JsonNode messageNode = value.path("messages").get(0);
                String numeroRemitente = messageNode.path("from").asText();
                String tipoMensaje = messageNode.path("type").asText();

                System.out.println("--- [Step 2] DATOS BASICOS ---");
                System.out.println("Remitente: " + numeroRemitente + " | Tipo: " + tipoMensaje);

                String mensajeOriginal = "text".equals(tipoMensaje) ? messageNode.path("text").path("body").asText() : "";

                Optional<Asesor> asesorOpt = asesorRepository.findAll().stream()
                        .filter(a -> numeroRemitente.equals(a.getTelefono()) || numeroRemitente.endsWith(a.getTelefono()))
                        .findFirst();

                if (asesorOpt.isPresent()) {
                    System.out.println("--- [Step 2.1] ES UN ASESOR REGISTRADO: " + asesorOpt.get().getNombre());

                }

                String estadoC = estadoUsuario.get(numeroRemitente);
                if (estadoC != null && estadoC.startsWith("MODO_HUMANO_")) {
                    System.out.println("--- [Step 2.2] CLIENTE EN MODO HUMANO ---");
                    return "EVENT_RECEIVED";
                }

                System.out.println("--- [Step 3] DERIVANDO A LOGICA DEL BOT ---");
                return procesarLogicaBot(value, messageNode, numeroRemitente, tipoMensaje, mensajeOriginal, "");
            } else {
                System.out.println("ℹ️ El payload no contiene mensajes (puede ser un status/read receipt)");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO EN recibirMensaje: " + e.getMessage());
            e.printStackTrace();
        }
        return "EVENT_RECEIVED";
    }
*/
/*FUNCIONA CORRECTAMENTE SIN EL ESTATUS DIA 23
    @PostMapping
    public String recibirMensaje(@RequestBody String payload) {
        System.out.println("\n--- [Step 1] PAYLOAD RECIBIDO ---");
        System.out.println(payload);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);

            JsonNode entry = root.path("entry").get(0);
            if (entry == null) {
                System.out.println("⚠️ Entry es nulo");
                return "EVENT_RECEIVED";
            }

            JsonNode value = entry.path("changes").get(0).path("value");

            if (value.has("messages")) {
                JsonNode messageNode = value.path("messages").get(0);
                String numeroRemitente = messageNode.path("from").asText();
                String tipoMensaje = messageNode.path("type").asText();

                System.out.println("--- [Step 2] DATOS BASICOS ---");
                System.out.println("Remitente: " + numeroRemitente + " | Tipo: " + tipoMensaje);

                String mensajeOriginal = "";
                if ("text".equals(tipoMensaje)) {
                    mensajeOriginal = messageNode.path("text").path("body").asText();
                } else if ("interactive".equals(tipoMensaje)) {
                    mensajeOriginal = messageNode.path("interactive").path("button_reply").path("title").asText();
                }

                Optional<Asesor> asesorOpt = asesorRepository.findAll().stream()
                        .filter(a -> numeroRemitente.equals(a.getTelefono()) ||
                                numeroRemitente.endsWith(a.getTelefono()) ||
                                extraer10Digitos(numeroRemitente).equals(extraer10Digitos(a.getTelefono())))
                        .findFirst();

                if (asesorOpt.isPresent()) {
                    System.out.println("--- [Step 2.1] ES UN ASESOR REGISTRADO: " + asesorOpt.get().getNombre());
                }

                String estadoC = estadoUsuario.get(numeroRemitente);
                if (estadoC != null && estadoC.startsWith("MODO_HUMANO_")) {
                    System.out.println("--- [Step 2.2] CLIENTE EN MODO HUMANO (" + numeroRemitente + ") ---");

                    return procesarLogicaBot(value, messageNode, numeroRemitente, tipoMensaje, mensajeOriginal, "");
                }

                System.out.println("--- [Step 3] DERIVANDO A LOGICA DEL BOT ---");
                return procesarLogicaBot(value, messageNode, numeroRemitente, tipoMensaje, mensajeOriginal, "");

            }
            else {
                System.out.println("ℹ️ El payload no contiene mensajes (puede ser un status/read receipt)");
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO EN recibirMensaje: " + e.getMessage());
            e.printStackTrace();
        }

        return "EVENT_RECEIVED";
    }
*/
@PostMapping
public String recibirMensaje(@RequestBody String payload) {
    System.out.println("\n--- [Step 1] PAYLOAD RECIBIDO ---");
    System.out.println(payload);

    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(payload);

        JsonNode entry = root.path("entry").get(0);
        if (entry == null) {
            System.out.println("⚠️ Entry es nulo");
            return "EVENT_RECEIVED";
        }

        JsonNode value = entry.path("changes").get(0).path("value");

        // --- CASO A: EL PAYLOAD CONTIENE UN MENSAJE (TEXTO O BOTÓN) ---
        if (value.has("messages")) {
            JsonNode messageNode = value.path("messages").get(0);
            String numeroRemitente = messageNode.path("from").asText();
            String tipoMensaje = messageNode.path("type").asText();
            String waId = messageNode.path("id").asText(); // ID del mensaje entrante

            System.out.println("--- [Step 2] DATOS BASICOS ---");
            System.out.println("Remitente: " + numeroRemitente + " | Tipo: " + tipoMensaje);

            String mensajeOriginal = "";
            if ("text".equals(tipoMensaje)) {
                mensajeOriginal = messageNode.path("text").path("body").asText();
            } else if ("interactive".equals(tipoMensaje)) {
                mensajeOriginal = messageNode.path("interactive").path("button_reply").path("title").asText();
            } else if ("button".equals(tipoMensaje)) {
                mensajeOriginal = messageNode.path("button").path("text").asText();
            }

            // Identificación de Asesor
            Optional<Asesor> asesorOpt = asesorRepository.findAll().stream()
                    .filter(a -> numeroRemitente.equals(a.getTelefono()) ||
                            numeroRemitente.endsWith(a.getTelefono()) ||
                            extraer10Digitos(numeroRemitente).equals(extraer10Digitos(a.getTelefono())))
                    .findFirst();

            if (asesorOpt.isPresent()) {
                System.out.println("--- [Step 2.1] ES UN ASESOR REGISTRADO: " + asesorOpt.get().getNombre());
            }

            // Lógica de Modo Humano / Asesor
            String estadoC = estadoUsuario.get(numeroRemitente);
            if (estadoC != null && estadoC.startsWith("MODO_HUMANO_")) {
                System.out.println("--- [Step 2.2] CLIENTE EN MODO HUMANO (" + numeroRemitente + ") ---");
                return procesarLogicaBot(value, messageNode, numeroRemitente, tipoMensaje, mensajeOriginal, waId);
            }

            System.out.println("--- [Step 3] DERIVANDO A LOGICA DEL BOT ---");
            return procesarLogicaBot(value, messageNode, numeroRemitente, tipoMensaje, mensajeOriginal, waId);

        }

        // --- CASO B: EL PAYLOAD CONTIENE UN STATUS (ENTREGADO/LEÍDO) ---
        // Esto es lo que alimenta tus gráficas en tiempo real
        else if (value.has("statuses")) {
            JsonNode statusNode = value.path("statuses").get(0);
            String wamid = statusNode.path("id").asText(); // El ID que guardamos al enviar
            String status = statusNode.path("status").asText(); // "delivered", "read", "failed"

            System.out.println("📈 ACTUALIZACIÓN DE MÉTRICAS: ID " + wamid + " -> " + status);

            // 1. Actualizar en tabla de Chats (Promociones individuales/Bot)
            chatRepository.findByMensajeIdWa(wamid).ifPresent(chat -> {
                chat.setEstadoWa(status);
                chatRepository.save(chat);
                System.out.println("✅ Estado actualizado en Chats");
            });

            // 2. Actualizar en tabla de EnvioLog (Campañas masivas)
            envioLogRepository.findByMensajeIdWa(wamid).ifPresent(log -> {
                log.setEstadoWa(status);
                envioLogRepository.save(log);
                System.out.println("✅ Estado actualizado en EnvioLog");
            });
        }

        else {
            System.out.println("ℹ️ El payload no contiene información procesable (sin mensajes ni estados)");
        }

    } catch (Exception e) {
        System.err.println("❌ ERROR CRÍTICO EN recibirMensaje: " + e.getMessage());
        e.printStackTrace();
    }

    return "EVENT_RECEIVED";
}

/*
    private void enviarPlantillaAsesor(String telefonoAsesor, String nombreAsesor, String nombreCliente, String telCliente, String campania, String landing) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + TOKEN);

            String nomCli = nombreCliente.replaceAll("[\\n\\r\\t]", " ").replaceAll("\\s{2,}", " ").trim();
            String camp = campania.replaceAll("[\\n\\r\\t]", " ").replaceAll("\\s{2,}", " ").trim();
            String land = landing.replaceAll("\\s", "");
            String telLimpio = telCliente.replaceAll("[^0-9]", "");

            String asesorSinEspacios = nombreAsesor.replace(" ", "_");

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", telefonoAsesor);
            body.put("type", "template");

            Map<String, Object> template = new HashMap<>();
            template.put("name", "notificacion_asesor");
            template.put("language", Collections.singletonMap("code", "en"));

            List<Map<String, Object>> components = new ArrayList<>();

            Map<String, Object> bodyComp = new HashMap<>();
            bodyComp.put("type", "body");
            bodyComp.put("parameters", Arrays.asList(
                    crearParam(nomCli),
                    crearParam(telLimpio),
                    crearParam(camp),
                    crearParam(land)
            ));
            components.add(bodyComp);

            Map<String, Object> btnComp = new HashMap<>();
            btnComp.put("type", "button");
            btnComp.put("sub_type", "url");
            btnComp.put("index", "0");

            String valBtn = telLimpio + "_ID_" + asesorSinEspacios;

            btnComp.put("parameters", Collections.singletonList(crearParam(valBtn)));
            components.add(btnComp);

            template.put("components", components);
            body.put("template", template);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String urlMeta = "https://graph.facebook.com/v21.0/1004648086076413/messages";

            ResponseEntity<String> response = restTemplate.postForEntity(urlMeta, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String msjParaHistorial = "Cliente: " + nomCli + " Tel:" + telLimpio;
                guardarHistorial(telefonoAsesor, "Sistema", msjParaHistorial, null, "bot");

                System.out.println("----------------------------------------------");
                System.out.println("📢 ALERTA ENVIADA Y REGISTRADA PARA: " + nombreAsesor);
                System.out.println("📦 Lead: " + nomCli + " (" + telLimpio + ")");
                System.out.println("----------------------------------------------");
            }

        } catch (Exception e) {
            System.err.println("❌ Error enviando plantilla: " + e.getMessage());
        }
    }
*/

    private void enviarPlantillaAsesor(String telefonoAsesor, String nombreAsesor, String nombreCliente, String telCliente, String campania, String landing) {
        System.out.println("--- [Step 5] PREPARANDO JSON PARA META ---");
        try {

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + TOKEN);
            String nAsesor = (nombreAsesor != null) ? nombreAsesor.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]", "").replaceAll("\\s{2,}", " ").trim() : "Asesor";
            String nCli = (nombreCliente != null) ? nombreCliente.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]", "").replaceAll("\\s{2,}", " ").trim() : "Cliente";
            String cAmp = (campania != null) ? campania.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]", "").replaceAll("\\s{2,}", " ").trim() : "General";
            String tLimpio = (telCliente != null) ? telCliente.replaceAll("[^0-9]", "") : "";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("to", telefonoAsesor);
            requestBody.put("type", "template");
            Map<String, Object> template = new HashMap<>();
            template.put("name", "aviso_asesor_v2");
            template.put("language", Collections.singletonMap("code", "en"));
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> bodyComp = new HashMap<>();
            bodyComp.put("type", "body");
            bodyComp.put("parameters", Arrays.asList(
                    crearParam(nAsesor), // {{1}}
                    crearParam(nCli), // {{2}}
                    crearParam(tLimpio), // {{3}}
                    crearParam(cAmp) // {{4}}
            ));
            components.add(bodyComp);
            template.put("components", components);
            requestBody.put("template", template);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String urlMeta = "https://graph.facebook.com/v21.0/1004648086076413/messages";
            System.out.println("📡 Enviando POST a Meta para: " + nAsesor + " (" + telefonoAsesor + ")");
            ResponseEntity<String> response = restTemplate.postForEntity(urlMeta, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {

                String responseBody = response.getBody();
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(responseBody);
                // Meta devuelve el ID en: messages[0].id
                String wamid = root.path("messages").get(0).path("id").asText();


                System.out.println("----------------------------------------------");
                System.out.println("📢 ALERTA ENVIADA EXITOSAMENTE A: " + nAsesor);
                System.out.println("----------------------------------------------");
                String msjParaHistorial = "Lead Notificado: " + nCli + " (" + tLimpio + ")";
                //guardarHistorial(telefonoAsesor, "Sistema", msjParaHistorial, null, "bot");
                guardarHistorial(telefonoAsesor, "Sistema", msjParaHistorial, wamid, "bot");
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ Error Meta (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Error General en enviarPlantillaAsesor: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private Map<String, String> crearParam(String texto) {
        Map<String, String> param = new HashMap<>();
        param.put("type", "text");
        param.put("text", (texto != null && !texto.isEmpty()) ? texto : "---");
        return param;
    }
/* funciona dia 22
private String procesarLogicaBot(JsonNode value, JsonNode messageNode, String numeroRemitente, String tipoMensaje, String mensajeOriginal, String waId) {
    System.out.println("--- [Step 4] ENTRANDO A procesarLogicaBot ---");
    System.out.println("📱 Remitente: " + numeroRemitente + " | Tipo: " + tipoMensaje);

    if (estadoUsuario.containsKey(numeroRemitente)) {
        String estado = estadoUsuario.get(numeroRemitente);

        if (estado.startsWith("MODO_HUMANO_")) {
            String telAsesor = estado.replace("MODO_HUMANO_", "");
            System.out.println("🔄 PUENTE ACTIVADO: Cliente -> Asesor (" + telAsesor + ")");

            String formatMensaje = "📱 *Mensaje del Cliente:* \n" + mensajeOriginal;
            whatsAppService.enviarMensaje(telAsesor, formatMensaje);

            guardarHistorial(numeroRemitente, "Cliente", mensajeOriginal, telAsesor, "bot");
            return "EVENT_RECEIVED";
        }

        if (estado.startsWith("MODO_ASESOR_")) {
            String telCliente = estado.replace("MODO_ASESOR_", "");

            if (mensajeOriginal.equalsIgnoreCase("Finalizar chat")) {
                String numAsesor10 = extraer10Digitos(numeroRemitente);
                Asesor as = asesorRepository.findByTelefono(numAsesor10).orElse(null);
                return finalizarChatHumano(as, telCliente, numeroRemitente);
            }

            System.out.println("🔄 PUENTE ACTIVADO: Asesor -> Cliente (" + telCliente + ")");
            whatsAppService.enviarMensaje(telCliente, mensajeOriginal);

            guardarHistorial(numeroRemitente, "Asesor", mensajeOriginal, telCliente, "bot");
            return "EVENT_RECEIVED";
        }
    }

    if ("button".equals(tipoMensaje)) {
        String buttonText = messageNode.path("button").path("text").asText();
        System.out.println("🔘 Botón detectado: [" + buttonText + "]");

        if (buttonText.equalsIgnoreCase("Me interesa")) {
            String num10 = extraer10Digitos(numeroRemitente);
            List<EnvioLog> logs = envioLogRepository.buscarUltimoPorDiezDigitos(num10, PageRequest.of(0, 1));

            String campRaw = (!logs.isEmpty()) ? logs.get(0).getCampaniaId() : "General";
            String camp = campRaw.replaceAll("[^a-zA-Z0-9 ]", "").trim();

            String landingRaw = (!logs.isEmpty() && logs.get(0).getUrlLanding() != null) ? logs.get(0).getUrlLanding() : "N/A";
            String landing = landingRaw.replaceAll("\\s+", "").trim();

            List<Asesor> asesores = asesorRepository.findAll();
            for (Asesor as : asesores) {
                enviarPlantillaAsesor(as.getTelefono(), as.getNombre(), "Cliente", numeroRemitente, camp, landing);
            }
            return "EVENT_RECEIVED";
        }

        if (buttonText.equalsIgnoreCase("Atender Lead")) {
            String numAsesor10 = extraer10Digitos(numeroRemitente);
            Optional<Asesor> asesorOpt = asesorRepository.findByTelefono(numAsesor10);

            if (asesorOpt.isPresent()) {
                Asesor as = asesorOpt.get();
                String telClienteFinal = null;

                Chats ultimoAviso = chatRepository.findFirstByTelefonoUsuarioAndMensajeContainingOrderByFechaEnvioDesc(
                        numeroRemitente, "Lead Notificado");

                if (ultimoAviso != null) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(([^)]+)\\)").matcher(ultimoAviso.getMensaje());
                    if (m.find()) {
                        String extraido = m.group(1).replaceAll("[^0-9]", "");
                        if (!extraido.contains(numAsesor10)) { telClienteFinal = extraido; }
                    }
                }

                if (telClienteFinal == null || telClienteFinal.isEmpty()) {
                    Page<EnvioLog> logsRecientes = envioLogRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "fechaEnvio")));
                    for (EnvioLog log : logsRecientes.getContent()) {
                        String candidato = log.getTelefonoDestino().replaceAll("[^0-9]", "");
                        if (!candidato.contains(numAsesor10)) {
                            telClienteFinal = candidato;
                            break;
                        }
                    }
                }

                if (telClienteFinal != null && telClienteFinal.length() >= 10) {
                    String clienteLimpio = extraer10Digitos(telClienteFinal);

                    if (clienteLimpio.equals(numAsesor10)) {
                        whatsAppService.enviarMensaje(numeroRemitente, "❌ Error: No puedes atenderte a ti mismo.");
                        return "EVENT_RECEIVED";
                    }

                    return procesarAsesorGanaLead(as, numeroRemitente, clienteLimpio);
                } else {
                    whatsAppService.enviarMensaje(numeroRemitente, "❌ Error: No se encontró un lead pendiente válido.");
                }
            }
        }
    }

    if ("text".equals(tipoMensaje)) {
        System.out.println("🔍 Validando acceso a Efecampañas para: " + numeroRemitente);

        String respuesta = whatsAppController.procesarMensaje(numeroRemitente, mensajeOriginal);
        whatsAppService.enviarMensaje(numeroRemitente, respuesta);
        return "EVENT_RECEIVED";
    }


    System.out.println("⚠️ Mensaje sin acción: El remitente no está en un puente ni presionó un botón válido.");
    return "EVENT_RECEIVED";
}
*/
    /* FUNCIONA ENVIA UNO Y 4 SON UN DELAY
private String procesarLogicaBot(JsonNode value, JsonNode messageNode, String numeroRemitente, String tipoMensaje, String mensajeOriginal, String waId) {
    System.out.println("--- [Step 4] ENTRANDO A procesarLogicaBot ---");
    String numLimpio10 = extraer10Digitos(numeroRemitente);

    if (estadoUsuario.containsKey(numeroRemitente)) {
        String estado = estadoUsuario.get(numeroRemitente);

        if (estado.startsWith("MODO_HUMANO_")) {
            String telAsesor = estado.replace("MODO_HUMANO_", "");
            whatsAppService.enviarMensaje(telAsesor, "📱 *Mensaje del Cliente:* \n" + mensajeOriginal);
            guardarHistorial(numeroRemitente, "Cliente", mensajeOriginal, telAsesor, "bot");
            return "EVENT_RECEIVED";
        }

        if (estado.startsWith("MODO_ASESOR_")) {
            String telCliente = estado.replace("MODO_ASESOR_", "");
            if (mensajeOriginal.equalsIgnoreCase("Finalizar chat")) {
                Asesor as = asesorRepository.findByTelefono(extraer10Digitos(numeroRemitente)).orElse(null);
                return finalizarChatHumano(as, telCliente, numeroRemitente);
            }
            whatsAppService.enviarMensaje(telCliente, mensajeOriginal);
            guardarHistorial(numeroRemitente, "Asesor", mensajeOriginal, telCliente, "bot");
            return "EVENT_RECEIVED";
        }
    }

    if ("button".equals(tipoMensaje) || "interactive".equals(tipoMensaje)) {

        String buttonText = "";
        if ("button".equals(tipoMensaje)) {
            buttonText = messageNode.path("button").path("text").asText();
        } else {
            buttonText = messageNode.path("interactive").path("button_reply").path("title").asText();
        }

        System.out.println("🔘 Botón detectado: [" + buttonText + "]");

        if (buttonText.equalsIgnoreCase("Más información")) {
            Optional<Promocion> promoOpt = promocionRepository.findFirstByTelefonoDestinoOrderByFechaCreacionDesc(numLimpio10);

            if (promoOpt.isPresent()) {
                Promocion p = promoOpt.get();
                String nombreWA = value.path("contacts").get(0).path("profile").path("name").asText("Cliente");

                for (Asesor as : asesorRepository.findAll()) {
                    enviarPlantillaAsesor(as.getTelefono(), as.getNombre(), nombreWA, numeroRemitente, p.getCategoria(), "Promoción IA");
                }

                whatsAppService.enviarMensaje(numeroRemitente, "¡Excelente! 🌟 He avisado a un asesor sobre tu interés en *" + p.getCategoria() + "*. En breve te atenderán.");

                String msjHistorial = "Lead Notificado: " + nombreWA + " (" + numLimpio10 + ")";
                guardarHistorial(numeroRemitente, "SISTEMA", msjHistorial, null, "bot");
            } else {
                System.out.println("⚠️ No se encontró registro en tabla 'promociones' para: " + numLimpio10);
            }
            return "EVENT_RECEIVED";
        }

        if (buttonText.equalsIgnoreCase("Me interesa")) {
            List<EnvioLog> logs = envioLogRepository.buscarUltimoPorDiezDigitos(numLimpio10, PageRequest.of(0, 1));

            String camp = (!logs.isEmpty()) ? logs.get(0).getCampaniaId().replaceAll("[^a-zA-Z0-9 ]", "").trim() : "General";
            String landing = (!logs.isEmpty() && logs.get(0).getUrlLanding() != null) ? logs.get(0).getUrlLanding().trim() : "N/A";

            for (Asesor as : asesorRepository.findAll()) {
                enviarPlantillaAsesor(as.getTelefono(), as.getNombre(), "Cliente", numeroRemitente, camp, landing);
            }

            whatsAppService.enviarMensaje(numeroRemitente, "¡Gracias por tu interés! Un asesor se pondrá en contacto contigo pronto.");

            String msjHistorial = "Lead Notificado: Cliente (" + numLimpio10 + ")";
            guardarHistorial(numeroRemitente, "SISTEMA", msjHistorial, null, "bot");
            return "EVENT_RECEIVED";
        }

        if (buttonText.equalsIgnoreCase("Atender Lead")) {
            String numAsesor10 = extraer10Digitos(numeroRemitente);
            Optional<Asesor> asesorOpt = asesorRepository.findByTelefono(numAsesor10);

            if (asesorOpt.isPresent()) {
                Asesor as = asesorOpt.get();
                String telClienteFinal = null;

                Page<Chats> avisos = chatRepository.findByMensajeContainingOrderByFechaEnvioDesc(
                        "Lead Notificado",
                        PageRequest.of(0, 1)
                );

                if (avisos != null && avisos.hasContent()) {
                    Chats ultimoAviso = avisos.getContent().get(0);

                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(([^)]+)\\)").matcher(ultimoAviso.getMensaje());
                    if (m.find()) {
                        telClienteFinal = m.group(1).replaceAll("[^0-9]", "");
                    }
                }

                if (telClienteFinal != null && telClienteFinal.length() >= 10) {
                    String clienteLimpio = extraer10Digitos(telClienteFinal);
                    System.out.println("🚀 Iniciando puente: Asesor " + as.getNombre() + " -> Cliente " + clienteLimpio);
                    return procesarAsesorGanaLead(as, numeroRemitente, clienteLimpio);
                } else {
                    whatsAppService.enviarMensaje(numeroRemitente, "❌ No encontré un lead reciente para asignar automáticamente.\n\n💡 *Tip:* Puedes escribir el número a 10 dígitos del cliente.");
                }
            }
            return "EVENT_RECEIVED";
        }
    }

    if ("text".equals(tipoMensaje)) {
        String respuesta = whatsAppController.procesarMensaje(numeroRemitente, mensajeOriginal);
        whatsAppService.enviarMensaje(numeroRemitente, respuesta);
        return "EVENT_RECEIVED";
    }

    return "EVENT_RECEIVED";
}
*/
private String procesarLogicaBot(JsonNode value, JsonNode messageNode, String numeroRemitente, String tipoMensaje, String mensajeOriginal, String waId) {
    System.out.println("--- [Step 4] ENTRANDO A procesarLogicaBot ---");
    String numLimpio10 = extraer10Digitos(numeroRemitente);

    if (estadoUsuario.containsKey(numeroRemitente)) {
        String estado = estadoUsuario.get(numeroRemitente);

        if (estado.startsWith("MODO_HUMANO_")) {
            String telAsesor = estado.replace("MODO_HUMANO_", "");
            whatsAppService.enviarMensaje(telAsesor, "📱 *Mensaje del Cliente:* \n" + mensajeOriginal);
            guardarHistorial(numeroRemitente, "Cliente", mensajeOriginal, telAsesor, "bot");
            return "EVENT_RECEIVED";
        }

        if (estado.startsWith("MODO_ASESOR_")) {
            String telCliente = estado.replace("MODO_ASESOR_", "");
            if (mensajeOriginal.equalsIgnoreCase("Finalizar chat")) {
                Asesor as = asesorRepository.findByTelefono(extraer10Digitos(numeroRemitente)).orElse(null);
                return finalizarChatHumano(as, telCliente, numeroRemitente);
            }
            whatsAppService.enviarMensaje(telCliente, mensajeOriginal);
            guardarHistorial(numeroRemitente, "Asesor", mensajeOriginal, telCliente, "bot");
            return "EVENT_RECEIVED";
        }
    }

    if ("button".equals(tipoMensaje) || "interactive".equals(tipoMensaje)) {

        String buttonText = "";
        if ("button".equals(tipoMensaje)) {
            buttonText = messageNode.path("button").path("text").asText();
        } else {
            buttonText = messageNode.path("interactive").path("button_reply").path("title").asText();
        }

        System.out.println("🔘 Botón detectado: [" + buttonText + "]");

        if (buttonText.equalsIgnoreCase("Más información")) {
            Optional<Promocion> promoOpt = promocionRepository.findFirstByTelefonoDestinoOrderByFechaCreacionDesc(numLimpio10);

            if (promoOpt.isPresent()) {
                Promocion p = promoOpt.get();
                p.setRespondido(true);
                promocionRepository.save(p);

                String nombreWA = value.path("contacts").get(0).path("profile").path("name").asText("Cliente");

                for (Asesor as : asesorRepository.findAll()) {
                    enviarPlantillaAsesor(as.getTelefono(), as.getNombre(), nombreWA, numeroRemitente, p.getCategoria(), "Promoción IA");
                }

                whatsAppService.enviarMensaje(numeroRemitente, "¡Excelente! 🌟 He avisado a un asesor sobre tu interés en *" + p.getCategoria() + "*. En breve te atenderán.");

                String msjHistorial = "Lead Notificado: " + nombreWA + " (" + numLimpio10 + ")";
                guardarHistorial(numeroRemitente, "SISTEMA", msjHistorial, null, "bot");
            } else {
                System.out.println("⚠️ No se encontró registro en tabla 'promociones' para: " + numLimpio10);
            }
            return "EVENT_RECEIVED";
        }

        if (buttonText.equalsIgnoreCase("Me interesa")) {
            List<EnvioLog> logs = envioLogRepository.buscarUltimoPorDiezDigitos(numLimpio10, PageRequest.of(0, 1));

            if (!logs.isEmpty()) {
                EnvioLog log = logs.get(0);

                log.setRespondido(true);
                envioLogRepository.save(log);

                String camp = log.getCampaniaId().replaceAll("[^a-zA-Z0-9 ]", "").trim();
                String landing = (log.getUrlLanding() != null) ? log.getUrlLanding().trim() : "N/A";

                for (Asesor as : asesorRepository.findAll()) {
                    enviarPlantillaAsesor(as.getTelefono(), as.getNombre(), "Cliente", numeroRemitente, camp, landing);
                }

                whatsAppService.enviarMensaje(numeroRemitente, "¡Gracias por tu interés! Un asesor se pondrá en contacto contigo pronto.");

                String msjHistorial = "Lead Notificado: Cliente (" + numLimpio10 + ")";
                guardarHistorial(numeroRemitente, "SISTEMA", msjHistorial, null, "bot");
            }
            return "EVENT_RECEIVED";
        }

        if (buttonText.equalsIgnoreCase("Atender Lead")) {
            String numAsesor10 = extraer10Digitos(numeroRemitente);
            Optional<Asesor> asesorOpt = asesorRepository.findByTelefono(numAsesor10);

            if (asesorOpt.isPresent()) {
                Asesor as = asesorOpt.get();
                String telClienteFinal = null;

                Page<Chats> avisos = chatRepository.findByMensajeContainingOrderByFechaEnvioDesc(
                        "Lead Notificado",
                        PageRequest.of(0, 1)
                );

                if (avisos != null && avisos.hasContent()) {
                    Chats ultimoAviso = avisos.getContent().get(0);

                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(([^)]+)\\)").matcher(ultimoAviso.getMensaje());
                    if (m.find()) {
                        telClienteFinal = m.group(1).replaceAll("[^0-9]", "");
                    }
                }

                if (telClienteFinal != null && telClienteFinal.length() >= 10) {
                    String clienteLimpio = extraer10Digitos(telClienteFinal);
                    System.out.println("🚀 Iniciando puente: Asesor " + as.getNombre() + " -> Cliente " + clienteLimpio);
                    return procesarAsesorGanaLead(as, numeroRemitente, clienteLimpio);
                } else {
                    whatsAppService.enviarMensaje(numeroRemitente, "❌ No encontré un lead reciente para asignar automáticamente.");
                }
            }
            return "EVENT_RECEIVED";
        }
    }

    if ("text".equals(tipoMensaje)) {
        String respuesta = whatsAppController.procesarMensaje(numeroRemitente, mensajeOriginal);
        whatsAppService.enviarMensaje(numeroRemitente, respuesta);


        return "EVENT_RECEIVED";
    }

    return "EVENT_RECEIVED";
}
private String extraer10Digitos(String telefono) {
        if (telefono == null) return "";
        String limpio = telefono.replaceAll("[^0-9]", "");
        return (limpio.length() >= 10) ? limpio.substring(limpio.length() - 10) : limpio;
    }
    /*
    private String procesarAsesorGanaLead(Asesor asesorActual, String numeroAsesor, String telCliente) {
        System.out.println("🔗 Conectando Asesor " + asesorActual.getNombre() + " con Cliente " + telCliente);

        String avisoBot = "¡Buenas noticias! ✨ El asesor *" + asesorActual.getNombre() + "* de Efectivale ha tomado tu solicitud.";
        whatsAppService.enviarMensaje(telCliente, avisoBot);

        String presentacion = "¡Hola! 👋 Soy *" + asesorActual.getNombre() + "*, tu asesor asignado. ¿En qué puedo apoyarte?";
        whatsAppService.enviarMensaje(telCliente, presentacion);

        asesorActual.setUltimoClienteAtendido(telCliente);
        asesorActual.setLeadsAtendidos(asesorActual.getLeadsAtendidos() + 1);
        asesorRepository.save(asesorActual);

        estadoUsuario.put(telCliente, "MODO_HUMANO_" + numeroAsesor);
        estadoUsuario.put(numeroAsesor, "MODO_ASESOR_" + telCliente);

        whatsAppService.enviarMensaje(numeroAsesor, "🏆 ¡Conectado! Ahora estás hablando con el cliente (" + telCliente + ").\n\nEscribe 'Finalizar chat' para terminar.");

        guardarHistorial(telCliente, "SISTEMA", "[Chat iniciado con asesor: " + asesorActual.getNombre() + "]", null, "bot");

        return "EVENT_RECEIVED";
    }*/

    private String procesarAsesorGanaLead(Asesor asesorActual, String numeroAsesor, String telCliente) {
        System.out.println("🔗 Conectando Asesor " + asesorActual.getNombre() + " con Cliente " + telCliente);

        String telClienteFull = telCliente.startsWith("52") ? telCliente : "521" + telCliente;

        String avisoBot = "¡Buenas noticias! ✨ El asesor *" + asesorActual.getNombre() + "* de Efectivale ha tomado tu solicitud.";
        whatsAppService.enviarMensaje(telClienteFull, avisoBot);

        String presentacion = "¡Hola! 👋 Soy *" + asesorActual.getNombre() + "*, tu asesor asignado. ¿En qué puedo apoyarte?";
        whatsAppService.enviarMensaje(telClienteFull, presentacion);

        asesorActual.setUltimoClienteAtendido(telCliente);
        asesorActual.setLeadsAtendidos(asesorActual.getLeadsAtendidos() + 1);
        asesorRepository.save(asesorActual);

        estadoUsuario.put(telClienteFull, "MODO_HUMANO_" + extraer10Digitos(numeroAsesor));
        estadoUsuario.put(numeroAsesor, "MODO_ASESOR_" + extraer10Digitos(telClienteFull));

        whatsAppService.enviarMensaje(numeroAsesor, "🏆 ¡Conectado! Ahora estás hablando con el cliente (" + telCliente + ").\n\nEscribe *'Finalizar chat'* para terminar la sesión.");

        guardarHistorial(telClienteFull, "SISTEMA", "[Chat iniciado con asesor: " + asesorActual.getNombre() + "]", null, "bot");

        System.out.println("✅ Puente establecido correctamente.");
        return "EVENT_RECEIVED";
    }

    /*
    private String procesarAsesorGanaLead(Asesor asesorActual, String numeroAsesor, String telCliente) {
        System.out.println("🔗 Conectando Asesor " + asesorActual.getNombre() + " con Cliente " + telCliente);
        String avisoBot = "¡Buenas noticias! ✨ El asesor *" + asesorActual.getNombre() + "* de Efectivale ha tomado tu solicitud.";
        whatsAppService.enviarMensaje(telCliente, avisoBot);
        String presentacion = "¡Hola! 👋 Soy *" + asesorActual.getNombre() + "*, tu asesor asignado. ¿En qué puedo apoyarte?";
        whatsAppService.enviarMensaje(telCliente, presentacion);
        asesorActual.setUltimoClienteAtendido(telCliente);
        asesorActual.setLeadsAtendidos(asesorActual.getLeadsAtendidos() + 1);
        asesorRepository.save(asesorActual);
        estadoUsuario.put(telCliente, "MODO_HUMANO_" + numeroAsesor);
        whatsAppService.enviarMensaje(numeroAsesor, "🏆 ¡Conectado! Ahora estás hablando con el cliente (" + telCliente + ").\n\nEscribe 'Finalizar chat' para terminar.");
        guardarHistorial(telCliente, "SISTEMA", "[Chat iniciado con asesor: " + asesorActual.getNombre() + "]", null, "bot");
        return "EVENT_RECEIVED";
    }*/
/*
    private String finalizarChatHumano(Asesor asesor, String telCliente, String telAsesor) {
        asesor.setUltimoClienteAtendido(null);
        asesorRepository.save(asesor);
        estadoUsuario.remove(telCliente);
        whatsAppService.enviarMensaje(telAsesor, "✅ Chat finalizado. El bot vuelve a tomar el control.");
        whatsAppService.enviarMensaje(telCliente, "La sesión con nuestro asesor ha terminado. ¡Gracias por contactarnos! 😊");
        return "EVENT_RECEIVED";
    }
*/

    private String finalizarChatHumano(Asesor asesor, String telCliente, String telAsesor) {
        if (asesor != null) {
            asesor.setUltimoClienteAtendido(null);
            asesorRepository.save(asesor);
        }
        estadoUsuario.remove(telCliente);
        estadoUsuario.remove(telAsesor);
        whatsAppService.enviarMensaje(telAsesor, "✅ Chat finalizado. El bot vuelve a tomar el control.");
        whatsAppService.enviarMensaje(telCliente, "La sesión con nuestro asesor ha terminado. ¡Gracias por contactarnos! 😊");
        System.out.println("🛑 Puente liberado: " + telAsesor + " <-> " + telCliente);
        return "EVENT_RECEIVED";
    }

    private void guardarHistorial(String tel, String nom, String msj, String id, String rem) {
        try {
            Chats c = new Chats();
            c.setTelefonoUsuario(tel);
            c.setNombreUsuario(nom);
            c.setMensaje(msj != null ? msj.replaceAll("[\\n\\r\\t]", " ").trim() : "");
            c.setMensajeIdWa(id);
            c.setRemitente(rem);
            c.setFechaEnvio(java.time.OffsetDateTime.now());
            chatRepository.save(c);
            System.out.println("💾 DB -> " + rem + " (" + tel + ")");
        } catch (Exception e) { System.err.println("❌ DB Error: " + e.getMessage()); }
    }

}