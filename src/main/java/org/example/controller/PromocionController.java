package org.example.controller;

import org.example.model.Promocion;
import org.example.repository.ChatRepository;
import org.example.repository.PromocionRepository;
import org.example.service.GeminiService;
import org.example.service.IAService;
import org.example.controller.WhatsAppController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.stream.Collectors;

import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/promociones")
@CrossOrigin(origins = "*")
public class PromocionController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private IAService imagenService;

    @Autowired
    private PromocionRepository promocionRepository;



    @Autowired
        private WhatsAppController whatsAppController;

    @Value("${gemini.api.key}")
    private String apiKey;
    private final String DIRECTORIO_CARGA = "/Users/aiengine/efecampana_media/promociones/";
    private final String URL_PUBLICA = "https://gaffe-skeptic-hangnail.ngrok-free.dev/promociones/";
    private final String RUTA_FISICA = "/Users/aiengine/IdeaProjects/efecampana/landings/promociones/";
/* SOLAMENTE GENERA UNA IMAGEN
    @PostMapping("/generar")
    public ResponseEntity<Map<String, String>> generar(@RequestBody Map<String, String> request) {
        String categoria = request.get("categoria");
        String publico = request.get("publico");

        String instruccionCopy = String.format(
                "Actúa como el Director de Estrategia de Efectivale. Redacta una 'Propuesta de Valor' magistral " +
                        "sobre %s para %s. El tono debe ser sofisticado, evocando poder, control financiero absoluto " +
                        "y superioridad frente al mercado. Destaca cómo blindamos su operación y maximizamos su flujo " +
                        "de efectivo de forma inteligente. Máximo 500 caracteres. " +
                        "No des opciones, solo el texto final + emoji de alto nivel.",
                categoria, publico);

        String copy = llamarGemini(instruccionCopy);

        String urlImagen = generarImagen(categoria, publico);

        Map<String, String> response = new HashMap<>();
        response.put("copy", copy);
        response.put("imagenUrl", urlImagen);
        response.put("categoria", categoria);

        return ResponseEntity.ok(response);
    }
*/
/* 22
    @PostMapping("/generar")
    public ResponseEntity<List<Map<String, String>>> generar(@RequestBody Map<String, String> request) {
        String categoria = request.get("categoria");
        String publico = request.get("publico");
        String tipo = request.getOrDefault("tipo", "IMAGEN");

        List<Map<String, String>> respuestaFinal = new ArrayList<>();
        String copyPrincipal = "";
        StringJoiner urlsConcatenadas = new StringJoiner(",");

        int cantidadAMensajes = "CARRUSEL".equals(tipo) ? 4 : 1;

        for (int i = 0; i < cantidadAMensajes; i++) {
            String instruccionCopy = String.format(
                    "Escribe un mensaje publicitario ultra-corto para WhatsApp sobre %s dirigido a %s. " +
                            "Usa un tono profesional y elegante. " +
                            "REGLA CRÍTICA: Máximo 20 palabras. Solo una frase impactante y un emoji. " +
                            "No incluyas introducciones ni títulos.",
                    categoria, publico);

            String copy = llamarGemini(instruccionCopy);
            String urlImagen = generarImagen(categoria, publico);

            if (i == 0) copyPrincipal = copy;
            urlsConcatenadas.add(urlImagen);

            Map<String, String> promo = new HashMap<>();
            promo.put("copy", copy);
            promo.put("imagenUrl", urlImagen);
            promo.put("categoria", categoria);
            respuestaFinal.add(promo);
        }

        try {
            Promocion nuevaPromo = new Promocion();
            nuevaPromo.setNombreCliente("Cliente");
            nuevaPromo.setCategoria(categoria);
            nuevaPromo.setTipoContenido(tipo);
            nuevaPromo.setCopyIa(copyPrincipal);
            nuevaPromo.setUrlsMultimedia(urlsConcatenadas.toString());

            promocionRepository.save(nuevaPromo);
            System.out.println("✅ Promoción guardada en DB ID: " + nuevaPromo.getId());

            respuestaFinal.get(0).put("idPromo", nuevaPromo.getId().toString());
        } catch (Exception e) {
            System.err.println("❌ Error DB: " + e.getMessage());
        }

        return ResponseEntity.ok(respuestaFinal);
    }
*/

    /*
@PostMapping("/generar")
public ResponseEntity<List<Map<String, String>>> generar(@RequestBody Map<String, String> request) {
    String categoria = request.get("categoria");
    String publico = request.get("publico");
    String tipo = request.getOrDefault("tipo", "IMAGEN");

    List<Map<String, String>> respuestaFinal = new ArrayList<>();
    StringJoiner urlsConcatenadas = new StringJoiner(",");

    int cantidadAMensajes = "CARRUSEL".equals(tipo) ? 4 : 1;

    for (int i = 0; i < cantidadAMensajes; i++) {
        String instruccionCopy = String.format(
                "Escribe un mensaje publicitario ultra-corto para WhatsApp sobre %s dirigido a %s. " +
                        "Usa un tono profesional y elegante. " +
                        "REGLA CRÍTICA: Máximo 20 palabras. Solo una frase impactante y un emoji. " +
                        "No incluyas introducciones ni títulos.",
                categoria, publico);

        String copy = llamarGemini(instruccionCopy);
        String urlImagen = generarImagen(categoria, publico);

        urlsConcatenadas.add(urlImagen);

        Map<String, String> promo = new HashMap<>();
        promo.put("copy", copy);
        promo.put("imagenUrl", urlImagen);
        promo.put("categoria", categoria);
        respuestaFinal.add(promo);
    }

    return ResponseEntity.ok(respuestaFinal);
}
*/


    @PostMapping("/generar")
    public ResponseEntity<List<Map<String, String>>> generar(@RequestBody Map<String, Object> request) {
        String categoria = (String) request.get("categoria");
        String publico = (String) request.get("publico");
        String tipo = (String) request.getOrDefault("tipo", "IMAGEN");

        Boolean usarImagenManual = (Boolean) request.getOrDefault("usarImagenManual", false);

        List<Map<String, String>> respuestaFinal = new ArrayList<>();
        int cantidadAMensajes = "CARRUSEL".equals(tipo) ? 4 : 1;

        for (int i = 0; i < cantidadAMensajes; i++) {
            String instruccionCopy = String.format(
                    "Actúa como un experto en marketing para %s. Escribe un mensaje persuasivo para WhatsApp " +
                            "dirigido a %s. REGLAS: Usa entre 10 y 20 palabras. Incluye un beneficio claro y un emoji. " +
                            "No respondas con confirmaciones, solo escribe el anuncio directo.",
                    categoria, publico);

            String copy = llamarGemini(instruccionCopy);
            String urlImagen = "";
            if (usarImagenManual) {
                urlImagen = "MANUAL_PLACEHOLDER";
            } else {
                urlImagen = generarImagen(categoria, publico);
            }

            Map<String, String> promo = new HashMap<>();
            promo.put("copy", copy);
            promo.put("imagenUrl", urlImagen);
            promo.put("categoria", categoria);
            respuestaFinal.add(promo);
        }
        return ResponseEntity.ok(respuestaFinal);
    }


    private String extraer10Digitos(String telefono) {
        if (telefono == null) return "";
        String limpio = telefono.replaceAll("[^0-9]", "");
        return (limpio.length() >= 10) ? limpio.substring(limpio.length() - 10) : limpio;
    }

    private String llamarGemini(String mensaje) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> part = new HashMap<>();
            part.put("text", mensaje);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> body = new HashMap<>();
            body.put("contents", Collections.singletonList(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            List candidates = (List) response.getBody().get("candidates");
            Map first = (Map) candidates.get(0);
            Map contentResp = (Map) first.get("content");
            List parts = (List) contentResp.get("parts");
            Map textPart = (Map) parts.get(0);

            return textPart.get("text").toString().trim();
        } catch (Exception e) {
            return "Beneficio exclusivo con Efectivale.";
        }
    }

public String generarImagen(String categoria, String publico) {
    String urlApi = "https://generativelanguage.googleapis.com/v1beta/models/imagen-4.0-generate-001:predict?key=" + apiKey;
    String escenaEspecifica;
    String cat = (categoria != null) ? categoria.toLowerCase() : "general";

    if (cat.contains("comida") || cat.contains("restaurante")) {
        escenaEspecifica = "una mesa de madera rústica con un plato gourmet humeante, una copa de vino y cubiertos elegantes en un restaurante cálido";
    } else if (cat.contains("combustible") || cat.contains("gasolina")) {
        escenaEspecifica = "una perspectiva dinámica de una estación de servicio moderna bajo la luz dorada del atardecer con reflejos realistas en el pavimento";
    } else if (cat.contains("despensa") || cat.contains("super") || cat.contains("mandado")) {
        escenaEspecifica = "un estante de cocina de diseño minimalista con ingredientes frescos, frutas orgánicas y frascos de cristal iluminados por luz natural";
    } else {
        escenaEspecifica = "un entorno corporativo moderno, luminoso y de alta gama con detalles de cristal y plantas de interior";
    }
    String promptEspañol = String.format(
            "Fotografía publicitaria estilo lifestyle cinematográfico, calidad 8k. " +
                    "ESCENA: Una toma única, vibrante y equilibrada de %s. " +
                    "INTEGRACIÓN DE MARCA: En un rincón de la composición, la palabra 'EFECTIVALE' en tipografía Sans-Serif moderna y color rojo (#be0f34), " +
                    "integrada sutilmente con la luz ambiental. " +
                    "ATMÓSFERA: Mucha profundidad de campo (bokeh suave), texturas fotorrealistas y una iluminación profesional que transmite calidad premium.",
            escenaEspecifica);

    Map<String, Object> requestBody = new HashMap<>();

    List<Map<String, Object>> instances = new ArrayList<>();
    Map<String, Object> instance = new HashMap<>();
    instance.put("prompt", promptEspañol);
    instances.add(instance);

    requestBody.put("instances", instances);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("sampleCount", 1);
    requestBody.put("parameters", parameters);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
    RestTemplate restTemplate = new RestTemplate();
    try {
        System.out.println("🚀 Generando con Imagen 4 para: " + categoria);
        ResponseEntity<Map> response = restTemplate.postForEntity(urlApi, entity, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("predictions")) {
            List<Map<String, Object>> predictions = (List<Map<String, Object>>) response.getBody().get("predictions");

            String base64Image = (String) predictions.get(0).get("bytesBase64Encoded");
            return guardarImagenEnDisco(base64Image);
        }
        return URL_PUBLICA + "fallback.png";
    } catch (HttpClientErrorException e) {
        System.err.println("❌ ERROR DE GOOGLE 4.0: " + e.getResponseBodyAsString());
        return URL_PUBLICA + "fallback.png";
    } catch (Exception e) {
        e.printStackTrace();
        return URL_PUBLICA + "fallback.png";
    }
}

    private String guardarImagenEnDisco(String base64Data) {
        try {
            String pureBase64 = base64Data.contains(",") ? base64Data.split(",")[1] : base64Data;
            byte[] imageBytes = Base64.getDecoder().decode(pureBase64);

            String nombreArchivo = "ia_promo_" + System.currentTimeMillis() + ".png";

            File directorio = new File(RUTA_FISICA);
            if (!directorio.exists()) directorio.mkdirs();

            Path path = Paths.get(RUTA_FISICA + nombreArchivo);
            Files.write(path, imageBytes);

            System.out.println("✅ Nueva imagen de IA guardada: " + nombreArchivo);
            return URL_PUBLICA + nombreArchivo;
        } catch (Exception e) {
            return URL_PUBLICA + "fallback.png";
        }
    }

    public void enviarPromocionWhatsApp(String telefonoCliente, String nombreCliente, String categoria) {
        String urlMeta = "https://graph.facebook.com/v21.0/1004648086076413/messages" + apiKey;


        String urlImagenIA = generarImagen(categoria, nombreCliente);
        String copyIA = generarCopyConGemini(categoria);

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", telefonoCliente);
        payload.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", "promociones_ia_carrusel");

        Map<String, String> language = new HashMap<>();
        language.put("code", "en");
        template.put("language", language);

        List<Map<String, Object>> components = new ArrayList<>();

        Map<String, Object> mainBody = new HashMap<>();
        mainBody.put("type", "body");
        mainBody.put("parameters", Arrays.asList(
                crearParametroTexto(nombreCliente), // {{1}}
                crearParametroTexto(categoria)      // {{2}}
        ));
        components.add(mainBody);

        // --- COMPONENTE CAROUSEL (La Card con la imagen y {{3}}) ---
        Map<String, Object> carousel = new HashMap<>();
        carousel.put("type", "carousel");

        List<Map<String, Object>> cards = new ArrayList<>();
        Map<String, Object> card = new HashMap<>();
        card.put("card_index", 0);

        List<Map<String, Object>> cardComponents = new ArrayList<>();

        Map<String, Object> cardHeader = new HashMap<>();
        cardHeader.put("type", "header");
        Map<String, Object> imageParam = new HashMap<>();
        imageParam.put("type", "image");
        Map<String, String> imageDetails = new HashMap<>();
        imageDetails.put("link", urlImagenIA);
        imageParam.put("image", imageDetails);
        cardHeader.put("parameters", Collections.singletonList(imageParam));
        cardComponents.add(cardHeader);

        Map<String, Object> cardBody = new HashMap<>();
        cardBody.put("type", "body");
        cardBody.put("parameters", Collections.singletonList(crearParametroTexto(copyIA)));
        cardComponents.add(cardBody);

        card.put("components", cardComponents);
        cards.add(card);
        carousel.put("cards", cards);
        components.add(carousel);

        template.put("components", components);
        payload.put("template", template);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        new RestTemplate().postForEntity(urlMeta, entity, String.class);
    }


    public String generarCopyConGemini(String categoria) {
        String prompt = "Escribe un beneficio corto de usar vales de " + categoria +
                " con Efectivale. Máximo 10 palabras.";

        return llamarGemini(prompt);
    }

    @Value("${meta.whatsapp.token}")
    private String metaToken;
/*
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, String>> enviarAMeta(@RequestBody Map<String, String> datos) {

        String telefono = datos.get("telefono");
        String categoria = datos.getOrDefault("categoria", "Promoción");
        String copyIA = datos.get("copyIA");
        String imagenUrl = datos.get("imagenUrl");
        String nombreCliente = datos.getOrDefault("nombre", "Cliente");

        String urlMeta = "https://graph.facebook.com/v21.0/1004648086076413/messages";

        Map<String, Object> bodyRequest = new HashMap<>();
        bodyRequest.put("messaging_product", "whatsapp");
        bodyRequest.put("to", telefono);
        bodyRequest.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", "promociones_ia_carrusel");
        template.put("language", Collections.singletonMap("code", "en_US"));

        List<Map<String, Object>> components = new ArrayList<>();

        Map<String, Object> bodyComp = new HashMap<>();
        bodyComp.put("type", "body");

        List<Map<String, Object>> bodyParams = new ArrayList<>();
        bodyParams.add(crearParamTexto(nombreCliente)); // {{1}}
        bodyParams.add(crearParamTexto(categoria));     // {{2}}
        bodyParams.add(crearParamTexto(copyIA));        // {{3}}

        bodyComp.put("parameters", bodyParams);
        components.add(bodyComp);

        Map<String, Object> carouselComp = new HashMap<>();
        carouselComp.put("type", "carousel");

        Map<String, Object> card = new HashMap<>();
        card.put("card_index", 0);
        List<Map<String, Object>> cardComponents = new ArrayList<>();

        Map<String, Object> cardHeader = new HashMap<>();
        cardHeader.put("type", "header");

        Map<String, Object> imageParam = new HashMap<>();
        imageParam.put("type", "image");
        Map<String, String> imageLink = new HashMap<>();
        imageLink.put("link", imagenUrl);
        imageParam.put("image", imageLink);

        cardHeader.put("parameters", Collections.singletonList(imageParam));
        cardComponents.add(cardHeader);

        Map<String, Object> cardBody = new HashMap<>();
        cardBody.put("type", "body");
        cardBody.put("parameters", Collections.singletonList(crearParamTexto(copyIA)));
        cardComponents.add(cardBody);

        card.put("components", cardComponents);
        carouselComp.put("cards", Collections.singletonList(card));
        components.add(carouselComp);

        template.put("components", components);
        bodyRequest.put("template", template);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(metaToken.trim());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyRequest, headers);

            System.out.println("📤 Enviando a " + telefono + " | Imagen: " + imagenUrl);

            ResponseEntity<String> response = new RestTemplate().postForEntity(urlMeta, entity, String.class);
            return ResponseEntity.ok(Collections.singletonMap("status", "Mensaje enviado con éxito"));

        } catch (HttpClientErrorException e) {
            System.err.println("❌ Error API Meta (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Collections.singletonMap("error", e.getResponseBodyAsString()));
        } catch (Exception e) {
            System.err.println("❌ Error crítico: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", "Error interno del servidor"));
        }
    }
*/
/* 22
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarCampania(@RequestBody Map<String, Object> request) {
        List<String> contactos = (List<String>) request.get("contactos");
        Map<String, String> contenido = (Map<String, String>) request.get("contenido");
        String tipo = contenido.get("tipoContenido");
        int exitosos = 0;
        int fallidos = 0;
        List<String> detalleErrores = new ArrayList<>();

        for (String telefono : contactos) {
            try {
                ejecutarEnvioMeta(telefono, contenido);
                exitosos++;
                if (contactos.size() > 1) Thread.sleep(250);
            } catch (Exception e) {
                fallidos++;
                detalleErrores.add("Error en " + telefono + ": " + e.getMessage());
                System.err.println("Fallo en envío a " + telefono + ": " + e.getMessage());
            }
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("exitosos", exitosos);
        respuesta.put("fallidos", fallidos);
        respuesta.put("detalles", detalleErrores);
        if (exitosos == 0 && !contactos.isEmpty()) {
            return ResponseEntity.badRequest().body(respuesta);
        }
        return ResponseEntity.ok(respuesta);
    }
*/

    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarCampania(@RequestBody Map<String, Object> request) {

        List<String> contactos = (List<String>) request.get("contactos");
        Map<String, String> contenido = (Map<String, String>) request.get("contenido");

        int exitosos = 0;
        int fallidos = 0;
        List<String> detalleErrores = new ArrayList<>();

        String copyLimpio = contenido.get("copyIA");
        if (copyLimpio != null) {
            copyLimpio = copyLimpio.replace("\n", " ")
                    .replace("\r", " ")
                    .replace("\t", " ")
                    .replaceAll("\\s{2,}", " ")
                    .trim();
            contenido.put("copyIA", copyLimpio);
        }

        String tipo = contenido.get("tipoContenido");

        for (String telefono : contactos) {
            try {
                String telLimpio = extraer10Digitos(telefono);

                Promocion nuevaPromo = new Promocion();
                nuevaPromo.setNombreCliente("Cliente");
                nuevaPromo.setTelefonoDestino(telLimpio);
                nuevaPromo.setCategoria(contenido.get("categoria"));
                nuevaPromo.setTipoContenido(tipo);
                nuevaPromo.setCopyIa(copyLimpio);
                nuevaPromo.setUrlsMultimedia(contenido.get("urlsMultimedia"));
                nuevaPromo.setEstado(true);
                nuevaPromo.setFechaCreacion(LocalDateTime.now());

                promocionRepository.save(nuevaPromo);
                ejecutarEnvioMeta(telefono, contenido);

                exitosos++;

                if (contactos.size() > 1) {
                    Thread.sleep(300);
                }

            } catch (Exception e) {
                fallidos++;
                String errorMsg = "Error en " + telefono + ": " + e.getMessage();
                detalleErrores.add(errorMsg);
                System.err.println("❌ Fallo en envío/guardado: " + errorMsg);
            }
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("exitosos", exitosos);
        respuesta.put("fallidos", fallidos);
        respuesta.put("detalles", detalleErrores);

        if (exitosos == 0 && !contactos.isEmpty()) {
            return ResponseEntity.badRequest().body(respuesta);
        }

        return ResponseEntity.ok(respuesta);
    }
/*
    private void ejecutarEnvioMeta(String telefono, Map<String, String> datos) throws Exception {
        String tipo = datos.getOrDefault("tipoContenido", "IMAGEN");
        String urlsRaw = datos.get("urlsMultimedia");

        if (urlsRaw == null || urlsRaw.isEmpty()) {
            throw new Exception("No se proporcionó URL de imagen.");
        }

        if ("CARRUSEL".equals(tipo)) {
            String[] urls = urlsRaw.split(",");
            for (String url : urls) {
                enviarMensajeImagenIndividual(telefono, datos, url.trim());

                Thread.sleep(800);
            }
        }
else {
            String urlUnica = urlsRaw.contains(",") ? urlsRaw.split(",")[0].trim() : urlsRaw.trim();
            enviarMensajeImagenIndividual(telefono, datos, urlUnica);
        }
    }*/

    private void ejecutarEnvioMeta(String telefono, Map<String, String> datos) throws Exception {
        String tipo = datos.getOrDefault("tipoContenido", "IMAGEN");
        String urlsRaw = datos.get("urlsMultimedia");

        if (urlsRaw == null || urlsRaw.isEmpty()) {
            throw new Exception("No se proporcionó URL de imagen.");
        }

        if ("CARRUSEL".equals(tipo)) {
            String[] urls = urlsRaw.split(",");
            for (String url : urls) {
                String urlLimpia = asegurarHttps(url.trim());
                enviarMensajeImagenIndividual(telefono, datos, urlLimpia);
                Thread.sleep(800);
            }
        } else {
            String urlUnica = urlsRaw.contains(",") ? urlsRaw.split(",")[0].trim() : urlsRaw.trim();
            urlUnica = asegurarHttps(urlUnica);
            enviarMensajeImagenIndividual(telefono, datos, urlUnica);
        }
    }

    private String asegurarHttps(String url) {
        if (url.startsWith("http://")) {
            return url.replace("http://", "https://");
        }
        return url;
    }
    private void enviarMensajeImagenIndividual(String telefono, Map<String, String> datos, String urlImagen) throws Exception {
        String urlMeta = "https://graph.facebook.com/v21.0/1004648086076413/messages";

        Map<String, Object> bodyRequest = new HashMap<>();
        bodyRequest.put("messaging_product", "whatsapp");
        bodyRequest.put("to", telefono);
        bodyRequest.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", "promociones_ia_carrusel");
        template.put("language", Collections.singletonMap("code", "en_US"));

        List<Map<String, Object>> components = new ArrayList<>();

        Map<String, Object> header = new HashMap<>();
        header.put("type", "header");
        Map<String, Object> imageDetails = new HashMap<>();
        imageDetails.put("link", urlImagen);
        header.put("parameters", Collections.singletonList(
                crearParamElemento("image", imageDetails)
        ));
        components.add(header);

        Map<String, Object> body = new HashMap<>();
        body.put("type", "body");
        body.put("parameters", Arrays.asList(
                crearParamTexto(datos.getOrDefault("nombre", "Cliente")),
                crearParamTexto(datos.getOrDefault("categoria", "Promoción")),
                crearParamTexto(datos.getOrDefault("copyIA", "Nueva oferta para ti"))
        ));
        components.add(body);

        template.put("components", components);
        bodyRequest.put("template", template);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(metaToken.trim());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyRequest, headers);
            new RestTemplate().postForEntity(urlMeta, entity, String.class);
        } catch (Exception e) {
            throw new Exception("Error en envío individual: " + e.getMessage());
        }
    }

    private Map<String, Object> crearParamElemento(String tipo, Object detalle) {
        Map<String, Object> p = new HashMap<>();
        p.put("type", tipo);
        p.put(tipo, detalle);
        return p;
    }

    private Map<String, Object> crearHeaderImagen(String url) {
        Map<String, Object> header = new HashMap<>();
        header.put("type", "header");

        Map<String, Object> imageDetails = new HashMap<>();
        imageDetails.put("link", url.trim());

        Map<String, Object> parameter = new HashMap<>();
        parameter.put("type", "image");
        parameter.put("image", imageDetails);

        header.put("parameters", Collections.singletonList(parameter));
        return header;
    }
    private Map<String, Object> crearHeaderImagenCard(String url) {
        Map<String, Object> component = new HashMap<>();
        component.put("type", "header");

        Map<String, Object> imageDetails = new HashMap<>();
        imageDetails.put("link", url.trim());

        Map<String, Object> parameter = new HashMap<>();
        parameter.put("type", "image");
        parameter.put("image", imageDetails);

        component.put("parameters", Collections.singletonList(parameter));

        return component;
    }
    private Map<String, Object> crearBodyCard(String texto) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", "body");

        Map<String, Object> param = new HashMap<>();
        param.put("type", "text");
        param.put("text", texto != null ? texto : "");

        body.put("parameters", Collections.singletonList(param));
        return body;
    }


    private Map<String, Object> crearParamTexto(String texto) {
        Map<String, Object> param = new HashMap<>();
        param.put("type", "text");
        param.put("text", texto != null ? texto : "");
        return param;
    }

    private Map<String, String> crearParametroTexto(String texto) {
        Map<String, String> param = new HashMap<>();
        param.put("type", "text");
        param.put("text", texto);
        return param;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        Map<String, String> respuesta = new HashMap<>();
        try {
            String nombreArchivo = guardarArchivo(file);

            respuesta.put("url", URL_PUBLICA + nombreArchivo);
            return ResponseEntity.ok(respuesta);

        } catch (IOException e) {
            respuesta.put("error", "Error al guardar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    private String guardarArchivo(MultipartFile file) throws IOException {
        String nombreOriginal = file.getOriginalFilename();
        String extension = "";

        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }

        String nuevoNombre = UUID.randomUUID().toString() + extension;
        Path rutaDestino = Paths.get(DIRECTORIO_CARGA).resolve(nuevoNombre);

        Files.createDirectories(rutaDestino.getParent());

        Files.copy(file.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

        return nuevoNombre;
    }
}
