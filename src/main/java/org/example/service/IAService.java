package org.example.service;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Base64;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.*;
import org.example.model.Campania;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONArray;
import okhttp3.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONException;
import java.util.concurrent.TimeUnit;
@Service
public class IAService {

    @Value("${gemini.api.key}")
    private String apiKey;

    public String analizarCampanias(String contexto) {

        String prompt =
                "Eres un experto en marketing digital y análisis de campañas.\n" +

                        "GENERA EXCLUSIVAMENTE HTML VALIDO.\n" +
                        "NO uses markdown.\n" +
                        "NO uses **, *, #, -, • ni símbolos especiales.\n" +
                        "NO escribas texto fuera de etiquetas HTML.\n" +
                        "TODO el contenido debe ir dentro de etiquetas HTML.\n" +
                        "Usa solo estas etiquetas: <h2>, <p>, <ol>, <li>, <div>.\n" +
                        "Cada párrafo debe ir dentro de <p>.\n" +
                        "Las listas deben usar <ol> y <li>.\n" +
                        "Nunca escribas números manualmente.\n" +
                        "Las listas deben generarse únicamente con <ol> y <li>.\n\n" +

                        "<div style='font-family:Arial;line-height:1.6;'>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>1. Mejores mensajes para WhatsApp</h2>\n" +
                        "<p>Genera entre 3 y 5 mensajes optimizados para conversión basados en las campañas.</p>\n" +
                        "<ol>\n" +
                        "<li>Mensaje optimizado</li>\n" +
                        "<li>Mensaje optimizado</li>\n" +
                        "<li>Mensaje optimizado</li>\n" +
                        "</ol>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>2. Nuevos textos de campaña</h2>\n" +
                        "<p>Genera entre 3 y 5 textos publicitarios atractivos para campañas digitales.</p>\n" +
                        "<ol>\n" +
                        "<li>Texto publicitario</li>\n" +
                        "<li>Texto publicitario</li>\n" +
                        "<li>Texto publicitario</li>\n" +
                        "</ol>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>3. Recomendación de cuándo lanzar campañas</h2>\n" +
                        "<p>Explica los mejores días de la semana para lanzar campañas basándote en el tipo de promoción.</p>\n" +
                        "<p>Describe también por qué esos días tienen mayor probabilidad de conversión.</p>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>4. Probabilidad de éxito estimada</h2>\n" +
                        "<p>Analiza cada campaña y asigna un porcentaje estimado de éxito considerando mensaje, duración y estado.</p>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>5. Recomendación de horarios</h2>\n" +
                        "<p>Sugiere los horarios ideales para enviar campañas considerando comportamiento del usuario.</p>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>6. Campaña con mayor efectividad</h2>\n" +
                        "<p>Indica cuál campaña tiene mayor probabilidad de éxito y explica por qué.</p>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>7. Campaña con menor rendimiento</h2>\n" +
                        "<p>Indica cuál campaña tiene menor rendimiento y explica las razones.</p>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>8. Mejora para cada campaña</h2>\n" +
                        "<p>Propón mejoras específicas para optimizar cada campaña.</p>\n" +
                        "<ol>\n" +
                        "<li>Mejora recomendada</li>\n" +
                        "<li>Mejora recomendada</li>\n" +
                        "<li>Mejora recomendada</li>\n" +
                        "</ol>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>9. Ideas nuevas de campañas</h2>\n" +
                        "<p>Genera 5 ideas nuevas de campañas incluyendo nombre, objetivo, mensaje y duración recomendada.</p>\n" +
                        "<ol>\n" +
                        "<li>Idea de campaña</li>\n" +
                        "<li>Idea de campaña</li>\n" +
                        "<li>Idea de campaña</li>\n" +
                        "<li>Idea de campaña</li>\n" +
                        "<li>Idea de campaña</li>\n" +
                        "</ol>\n" +

                        "<h2 style='text-align:center;margin-top:30px;'>10. Gráfica de probabilidad de éxito</h2>\n" +

                        "<p>Genera una gráfica de barras VERTICALES en HTML basada en las campañas analizadas:\n</p>\n" +

                        "<p>NombreCampaña - 50%</p>\n" +
                        "<div style='display:block;width:50%;height:20px;background-color:blue;margin-bottom:10px;'> </div>\n" +

                        "<p>Reglas obligatorias:</p>\n" +
                        "<p>1. Cada campaña debe tener un porcentaje estimado.</p>\n" +
                        "<p>2. El ancho del div debe coincidir exactamente con el porcentaje.</p>\n" +
                        "<p>3. Alterna los colores azul y rojo entre cada barra.</p>\n" +
                        "<p>4. Genera una barra por cada campaña.</p>\n" +
                        "<p>5. El div de la barra debe contener un espacio interno para que se renderice en PDF.</p>\n" +
                        "<p>6. Siempre debes generar el div de la barra después del porcentaje.</p>\n" +

                        "</div>\n\n" +

                        "Analiza las siguientes campañas y genera el reporte completo:\n" +
                        contexto;
        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + apiKey; RestTemplate restTemplate = new RestTemplate(); Map<String, Object> textPart = new HashMap<>(); textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>(); parts.put("parts", Arrays.asList(textPart));

        Map<String, Object> body = new HashMap<>(); body.put("contents", Arrays.asList(parts));

        HttpHeaders headers = new HttpHeaders();


        HttpEntity<Map<String,Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        List candidates = (List) response.getBody().get("candidates");

        Map candidate = (Map) candidates.get(0);

        Map content = (Map) candidate.get("content");

        List partsResp = (List) content.get("parts");

        Map textMap = (Map) partsResp.get(0);

        String resultado = textMap.get("text").toString();

        resultado = resultado
                .replace("**","")
                .replace("*","")
                .replace("•","")
                .replace("#","");

       return resultado;
    }

    public String generarLanding(Campania campania) {
        try {
            Map<String, String> gasolinaImgs = new HashMap<>();
            gasolinaImgs.put("PRODUCTO_TARJETA", "/resources/gasolina/01-EFC-COMBUSTIBLE.webp");
            gasolinaImgs.put("PRODUCTO_ECARD", "/resources/gasolina/03-efc-comb-ecard.webp");
            gasolinaImgs.put("ICONO_FUEL_REAL", "/resources/gasolina/ICONO FUEL.svg");

            Map<String, String> despensaImgs = new HashMap<>();
            despensaImgs.put("PRODUCTO_TARJETA", "/resources/despensa/02-EFC-DESPENSA-PLUS.webp");
            despensaImgs.put("ICONO_DESPENSA_REAL", "/resources/despensa/ICONO DESPENSA .svg");

            Map<String, String> comidaImgs = new HashMap<>();
            comidaImgs.put("PRODUCTO_TARJETA", "/resources/despensa/07-comida.webp");
            comidaImgs.put("ICONO_COMIDA_REAL", "/resources/despensa/ICONO COMIDA.svg");

            String logoEfectivale = "/resources/logos/logo efectivale blanco.svg";

            String nombre = campania.getNombre();
            String mensaje = (campania.getMensaje() != null) ? campania.getMensaje() : "Soluciones inteligentes para tu empresa.";
            int duracion = campania.getDuracionDias();
            int anioActual = java.time.Year.now().getValue();
            String tipo = "gasolina";
            String nombreLower = nombre.toLowerCase();

            Map<String, String> imagenesSeleccionadas;
            if (nombreLower.contains("despensa")) {
                tipo = "despensa";
                imagenesSeleccionadas = despensaImgs;
            } else if (nombreLower.contains("comida")) {
                tipo = "comida";
                imagenesSeleccionadas = comidaImgs;
            } else {
                imagenesSeleccionadas = gasolinaImgs;
            }

            StringBuilder prompt = new StringBuilder();
            prompt.append("Actúa como un Diseñador Frontend Senior experto en UI/UX Corporativo para Efectivale.\n")
                    .append("Genera una Landing Page ESPECTACULAR, moderna y de alto impacto.\n\n")

                    .append("DATOS OBLIGATORIOS (USAR EXACTAMENTE):\n")
                    .append("- TÍTULO: ").append(nombre).append("\n")
                    .append("- MENSAJE CENTRAL: ").append(mensaje).append("\n")
                    .append("- DURACIÓN: ").append(duracion).append(" días (Dato crítico).\n")
                    .append("- IDENTIFICADOR DE CAMPAÑA: ").append(nombre).append("\n")
                    .append("- AÑO: ").append(anioActual).append("\n")
                    .append("- COLORES: Primario #be0f34 (Rojo), Secundario #6d6e65 (Gris).\n\n")

                    .append("IMÁGENES REALES:\n");
            imagenesSeleccionadas.forEach((key, val) -> prompt.append("- ").append(key).append(": ").append(val).append("\n"));
            prompt.append("- LOGO_NAV: ").append(logoEfectivale).append("\n\n")

                    .append("ESTRUCTURA Y REQUISITOS TÉCNICOS:\n")
                    .append("1. **HEADER/NAV:** Logo visible con padding superior de 20px.\n")
                    .append("2. **CONTRASTE:** Texto blanco (#FFFFFF) sobre fondos oscuros (#be0f34 o #6d6e65).\n")
                    .append("3. **TABLA RESPONSIVA:** Tabla de beneficios con 'overflow-x: auto'.\n")
                    .append("4. **FORMULARIO (LÓGICA CRÍTICA):**\n")
                    .append("   - Campos obligatorios: Nombre, Empresa, Teléfono, Correo, Comentarios.\n")
                    .append("   - CAMPO OCULTO (OBLIGATORIO): <input type='hidden' name='nombre_campania' value='").append(nombre).append("'>\n")
                    .append("   - SCRIPT DE ENVÍO: Al hacer submit, usa event.preventDefault().\n")
                    .append("   - IMPORTANTE: Usa 'const data = new FormData(event.target);' y envíalo vía fetch() POST a '/api/landing/contacto'.\n")
                    .append("   - ÉXITO: Tras el fetch, muestra un Toast/notificación elegante y ejecuta 'event.target.reset()'.\n")
                    .append("5. **LENGUAJE:** Tono ejecutivo y persuasivo.\n")
                    .append("6. **IA VISUAL:** Fondo con: https://image.pollinations.ai/prompt/premium-business-").append(tipo).append("-texture-corporate-dark\n\n")
                    .append("REGLA DE ORO: No escribas NADA de texto explicativo. Empieza directamente con <!DOCTYPE html>.");
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build();

            JSONObject body = new JSONObject()
                    .put("contents", new JSONArray()
                            .put(new JSONObject().put("parts", new JSONArray()
                                    .put(new JSONObject().put("text", prompt.toString())))));

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                    .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            if (!json.has("candidates")) {
                System.err.println("Error de API: " + responseBody);
                return "<html><body>Error: La IA no pudo generar el contenido.</body></html>";
            }

            String htmlRaw = json.getJSONArray("candidates")
                    .getJSONObject(0).getJSONObject("content")
                    .getJSONArray("parts").getJSONObject(0).getString("text");

            htmlRaw = htmlRaw.replaceAll("```html", "").replaceAll("```", "").trim();

            int indexDoctype = htmlRaw.toLowerCase().indexOf("<!doctype");
            int indexHtml = htmlRaw.toLowerCase().indexOf("<html");
            int inicioReal = (indexDoctype != -1) ? indexDoctype : indexHtml;

            if (inicioReal != -1) {
                htmlRaw = htmlRaw.substring(inicioReal);
            }

            htmlRaw = htmlRaw.replace("\\/", "/").trim();

            Files.write(Paths.get("landing.html"), htmlRaw.getBytes(StandardCharsets.UTF_8));
            return htmlRaw;

        } catch (Exception e) {
            e.printStackTrace();
            return "<html><body>Error Crítico: " + e.getMessage() + "</body></html>";
        }
    }


}