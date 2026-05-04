package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Asesor;
import org.example.model.PersonalAutorizado;
import org.example.model.RespuestaCampania;
import org.example.repository.CampaniaRepository;
import org.example.repository.RespuestaCampaniaRepository;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.model.Campania;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.example.dto.EnvioMasivoDTO;

@RestController
@RequestMapping("/whatsapp")
@CrossOrigin(origins = "*")
public class WhatsAppController {

    @Autowired
    private BotService botService;

    @Autowired
    private CampaniaService campaniaService;

    @Autowired
    private IAService iaService;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private PersonalService personalService;

    @Autowired
    private CampaniaRepository campaniaRepository;

    @Autowired
    private RespuestaCampaniaRepository respuestaRepository;

    @Autowired
    private WhatsAppWebhookController WhatsAppWebhookController;

    @PostConstruct
    public void init() {
        pdfService.cargarPDF();
    }

    @Autowired
    private EmailService emailService;

    @Value("${gemini.api.key}")
    private String apiKey;

    private Map<String, String> estadoUsuario = new HashMap<>();
    private Map<String, String> nombreCampania = new HashMap<>();
    private Map<String, Integer> duracionCampania = new HashMap<>();
    private Map<String, String> mensajeCampania = new HashMap<>();
    private Map<String, String> transcripcionesTemporales = new HashMap<>();

    @GetMapping("/mensaje")
    public String recibirMensaje(@RequestParam String numero,
                                 @RequestParam String mensaje) {

        String respuesta = procesarMensaje(numero, mensaje);

        botService.enviarMensaje(numero, respuesta);

        return respuesta;
    }

    public String procesarMensaje(String numero, String mensaje) {

        String numeroOriginalMeta = numero;
        String diezDigitos = "";
        String estado;
        PersonalAutorizado p;
        String listaStr = "";
        mensaje = mensaje.toLowerCase().trim();
        if (numero != null && numero.length() >= 10) {
            diezDigitos = numero.substring(numero.length() - 10);
        }
        System.out.println("==========================================");
        System.out.println("📩 NUEVO MENSAJE RECIBIDO");
        System.out.println("📱 Número Original (Meta): " + numeroOriginalMeta);
        System.out.println("🎯 Buscando en DB por terminación: " + diezDigitos);
        System.out.println("💬 Mensaje del usuario: " + mensaje);

        p = personalService.buscarPorNumero(diezDigitos);

        if (p == null) {
            System.out.println("❌ RESULTADO: El número " + diezDigitos + " NO existe en la DB.");
            System.out.println("==========================================");
            return "🚫 Tu número no está registrado en el panel de Efectivale.";
        }

        System.out.println("✅ RESULTADO: Usuario encontrado -> " + p.getNombre());
        System.out.println("📧 Correo destino: " + p.getCorreo());

        estado = estadoUsuario.get(numeroOriginalMeta);
        System.out.println("🔄 Estado actual del flujo: " + estado);
        System.out.println("==========================================");

        if ("hola bot".equals(mensaje) || "hola".equals(mensaje)) {
            if ("AUTENTICADO".equals(estado)) {
                return "👋 ¡Hola de nuevo, " + p.getNombre() + "!\n\n" + obtenerMenuPrincipal();
            }

            if (p.isBloqueado()) {
                return "🚫 Tu acceso está bloqueado.";
            }

            String token = String.format("%06d", new Random().nextInt(999999));
            p.setTokenVerificacion(token);
            p.setTokenGeneradoAt(LocalDateTime.now());
            p.setIntentosFallidos(0);
            personalService.guardar(p);

            emailService.enviarToken(p.getCorreo(), token);

            estadoUsuario.put(numeroOriginalMeta, "ESPERANDO_TOKEN");
            return "👋 ¡Hola " + p.getNombre() + "! Bienvenido a Efecampañas.\n\n🔐 He enviado un código a tu correo: " + p.getCorreo() + "\nIngrésalo aquí para continuar.";
        }
        if ("ESPERANDO_TOKEN".equals(estado)) {
            if (p.getTokenGeneradoAt().plusHours(24).isBefore(LocalDateTime.now())) {
                estadoUsuario.remove(numeroOriginalMeta);
                return "⏰ El token ha vencido. Escribe *hola bot* de nuevo.";
            }

            if (mensaje.equals(p.getTokenVerificacion())) {
                estadoUsuario.put(numeroOriginalMeta, "AUTENTICADO");
                return "✅ ¡Acceso concedido!\n\n" + obtenerMenuPrincipal();
            } else {
                p.setBloqueado(true);
                personalService.guardar(p);
                estadoUsuario.remove(numeroOriginalMeta);
                return "❌ Token incorrecto. Por seguridad, tu acceso ha sido bloqueado.";
            }
        }

        if (estado == null) {
            if (mensaje.matches("\\d+")) {
                return "⚠️ Por favor, escribe *hola bot* para iniciar sesión y usar las opciones del menú.";
            }
        }

        if ("AUTENTICADO".equals(estado)) {
            if (mensaje.equals("1")) {
                estadoUsuario.put(numeroOriginalMeta, "CONFIRMAR_CREACION");
                return "🚀 Crear campaña\n\nUna campaña te permite enviar beneficios...\n\n👉 ¿Deseas continuar?\n1️⃣ Sí\n2️⃣ No";
            }
            if (mensaje.equals("2")) {
                estadoUsuario.put(numeroOriginalMeta, "REPORTE");
                List<Campania> activas = campaniaService.obtenerActivas();
                if (activas.isEmpty()) return "📊 No tienes campañas activas.";
                String lista = "";
                for (int i = 0; i < activas.size(); i++) lista += (i + 1) + "️⃣ " + activas.get(i).getNombre() + "\n";
                return "📊 Generar reporte\n\nSelecciona una campaña:\n\n" + lista + "0️⃣ Reporte general";
            }
            if (mensaje.equals("3")) {
                estadoUsuario.put(numeroOriginalMeta, "LANDING");
                List<Campania> activasLanding = campaniaService.obtenerActivas();
                if (activasLanding.isEmpty()) return "❌ No tienes campañas activas.";
                String listaLanding = "";
                for (int i = 0; i < activasLanding.size(); i++) listaLanding += (i + 1) + "️⃣ " + activasLanding.get(i).getNombre() + "\n";
                return "🚀 Crear landing\n\nSelecciona campaña:\n\n" + listaLanding;
            }
        }


        if (mensaje.matches(
                ".*\\b(" +
                        // Insultos comunes
                        "wey|guey|idiota|pendej[oa]|imbécil|estúpido|estupida|tonto|tonta|boludo|boluda|" +
                        "chinga[oa]?|ching[ao]?|cabr[oó]n|cabr[oó]na|maric[oó]n|maricona|put[oa]|prostituta|zorra|verga|perra|pito|pinga|culo|" +
                        "mamón|mamona|gilipollas|zoquete|tarado|tarada|hijo de puta|hijodeputa|" +
                        // Variantes con caracteres reemplazados
                        "p[uú]t[o0a@]+|c[a@]br[oó]n|ch[ií]ng[a@][oa]?|m[a@]r[ií]c[oó]n|h[ií]j[oó] d[eé] p[uú]t[a@]" +
                        ")\\b.*"
        )) {
            return "⚠️ Por favor mantén el respeto.\n\nSoy un asistente de Efectivale y solo puedo ayudarte con información sobre:\n- campañas\n- vales\n- gasolina\n- despensa\n\nEscribe *hola* para comenzar.";
        }
        if (mensaje.matches(".*(jaja|xd|meme|gif).*")) {
            return "🙂 Soy un asistente de Efectivale.\nSolo puedo ayudarte con campañas, vales y reportes.\n\nEscribe *hola* para comenzar.";
        }
      /*  if (mensaje.replaceAll("[^a-zA-Z0-9]", "").isEmpty()) {
            return "🙂 Por favor escribe un mensaje válido.\nPuedes escribir *hola* para ver el menú.";
        }*/




        if ("PROCESANDO".equals(estado)) {
            return "⏳ Estoy procesando tu solicitud, por favor espera unos segundos...";
        }
        boolean esPregunta = mensaje.contains("como") || mensaje.contains("cómo") ||
                mensaje.contains("que") || mensaje.contains("qué") ||
                mensaje.contains("ayuda") || mensaje.contains("explica");

        boolean esReporte = mensaje.contains("reporte") &&
                (mensaje.contains("crear") || mensaje.contains("hacer") ||
                        mensaje.contains("generar") || mensaje.contains("quiero") ||
                        mensaje.contains("necesito"));

        boolean esLanding = (mensaje.contains("landing") || mensaje.contains("campaña")) &&
                (mensaje.contains("crear") || mensaje.contains("hacer") ||
                        mensaje.contains("quiero") || mensaje.contains("necesito"));

        boolean esCrearCampania = mensaje.contains("quiero") && mensaje.contains("campaña");


        if ("ACTIVAR_POR_NUMERO".equals(estado)) {
            try {
                int opcion = Integer.parseInt(mensaje) - 1;
                List<Campania> inactivas = campaniaService.obtenerInactivas();
                if (opcion >= 0 && opcion < inactivas.size()) {
                    String nombre = inactivas.get(opcion).getNombre();

                    campaniaService.activar(nombre, numero);

                    estadoUsuario.remove(numero);
                    return "Procesando activación...";
                }
            } catch (Exception e) { }
            return "❌ Selección inválida.";
        }
        if ("DESACTIVAR_POR_NUMERO".equals(estado)) {
            try {
                int opcion = Integer.parseInt(mensaje) - 1;
                List<Campania> activas = campaniaService.obtenerActivas();
                if (opcion >= 0 && opcion < activas.size()) {
                    Campania seleccionada = activas.get(opcion);
                    campaniaService.desactivar(seleccionada.getNombre());
                    estadoUsuario.remove(numero);
                    return "🔴 La campaña \"" + seleccionada.getNombre() + "\" ha sido desactivada.";
                }
            } catch (Exception ignored) {}
            return "❌ Selección inválida. Elige un número de la lista o escribe *hola*.";
        }

        if ("CONFIRMAR_CREACION".equals(estado)) {
            if (mensaje.equals("1")) {
                estadoUsuario.put(numero, "NOMBRE");
                return "Perfecto 👍\n\n¿Cuál es el nombre de la campaña?";
            } else {
               // estadoUsuario.remove(numero);
                //return "Ok 👍\nPuedes escribir *hola* para volver al menú.";
                estadoUsuario.put(numeroOriginalMeta, "AUTENTICADO");
                return "Ok 👍 Regresamos al menú.\n\n" + obtenerMenuPrincipal();
            }
        }

        if ("NOMBRE".equals(estado)) {
            nombreCampania.put(numero, mensaje);
            estadoUsuario.put(numero, "DURACION");
            return "¿Cuántos días durará la campaña?";
        }
        if ("DURACION".equals(estado)) {
            try {
                int duracion = parseDuracionADias(mensaje);
                //int duracion = Integer.parseInt(mensaje);
                duracionCampania.put(numero, duracion);
                estadoUsuario.put(numero, "MENSAJE");
                return "Escribe el mensaje que quieres enviar a los clientes.";
            } catch (Exception e) {
                return "Por favor, escribe un número válido para la duración.";
            }
        }
        if ("MENSAJE".equals(estado)) {

            estadoUsuario.put(numero, "PROCESANDO");
            botService.enviarMensaje(numero, "⏳ Procesando tu campaña...");

            try {
                mensajeCampania.put(numero, mensaje);

                Campania c = campaniaService.crear(
                        nombreCampania.get(numero),
                        duracionCampania.get(numero),
                        mensaje,
                        null,
                        numero
                );

                estadoUsuario.put(numero, "CONFIRMAR");

                return "⏳ Tu campaña \"" + c.getNombre() + "\" está en revisión. " +
                        "Un supervisor la revisará pronto. " +
                        "Si es aprobada, se activará y se generará la landing.";

            } catch (Exception e) {
                estadoUsuario.remove(numero);
                return "❌ Ocurrió un error al crear la campaña. Intenta de nuevo.";
            }
        }

        if (estado == null) {
            if (esCrearCampania) {
                estadoUsuario.put(numero, "CONFIRMAR_CREACION");
                return "🚀 Crear campaña\n\nUna campaña te permite enviar beneficios como vales de gasolina, despensa, etc.\n\n👉 ¿Deseas continuar?\n\n1️⃣ Sí\n2️⃣ No";
            }

            if (esReporte) {
                estadoUsuario.put(numero, "REPORTE");
                List<Campania> activas = campaniaService.obtenerActivas();
                if (activas.isEmpty()) return "📊 No tienes campañas activas.";

                String lista = "";
                for (int i = 0; i < activas.size(); i++) lista += (i + 1) + "️⃣ " + activas.get(i).getNombre() + "\n";
                return "📊 Generar reporte\n\nSelecciona una campaña:\n\n" + lista + "0️⃣ Reporte general";
            }

            if (esLanding) {
                estadoUsuario.put(numero, "LANDING");
                List<Campania> activas = campaniaService.obtenerActivas();
                if (activas.isEmpty()) return "❌ No tienes campañas activas.";

                String lista = "";
                for (int i = 0; i < activas.size(); i++) lista += (i + 1) + "️⃣ " + activas.get(i).getNombre() + "\n";
                return "🚀 Crear landing\n\nSelecciona campaña:\n\n" + lista;
            }

            if (esPregunta) {
                if (mensaje.contains("reporte"))
                    return llamarGemini("Explica qué es un reporte de campaña y cómo interpretarlo.\n\nUsuario: " + mensaje);
                if (mensaje.contains("landing"))
                    return llamarGemini("Explica qué es una landing page y cómo crearla.\n\nUsuario: " + mensaje);
                if (mensaje.contains("campaña"))
                    return llamarGemini("Explica qué es una campaña y cómo crearla.\n\nUsuario: " + mensaje);
            }
/*
            switch (mensaje) {
                case "1":
                    estadoUsuario.put(numero, "CONFIRMAR_CREACION");
                    return "🚀 Crear campaña\n\nUna campaña te permite enviar beneficios...\n1️⃣ Sí\n2️⃣ No";
/*
                case "2":
                    List<Campania> inactivas = campaniaService.obtenerInactivas();
                    if (inactivas.isEmpty()) return "No hay campañas inactivas para activar.";
                    estadoUsuario.put(numero, "ACTIVAR_POR_NUMERO");
                    listaStr = "🟢 Selecciona la campaña a ACTIVAR:\n\n";
                    for (int i = 0; i < inactivas.size(); i++) listaStr += (i + 1) + "️⃣ " + inactivas.get(i).getNombre() + "\n";
                    return listaStr;

                case "3":
                    List<Campania> activasDes = campaniaService.obtenerActivas();
                    if (activasDes.isEmpty()) return "No hay campañas activas para desactivar.";
                    estadoUsuario.put(numero, "DESACTIVAR_POR_NUMERO");
                    listaStr = "🔴 Selecciona la campaña a DESACTIVAR:\n\n";
                    for (int i = 0; i < activasDes.size(); i++) listaStr += (i + 1) + "️⃣ " + activasDes.get(i).getNombre() + "\n";
                    return listaStr;*/

                /*case "2":
                    estadoUsuario.put(numero, "REPORTE");
                    List<Campania> activas = campaniaService.obtenerActivas();
                    if (activas.isEmpty()) return "📊 No tienes campañas activas.";
                    String lista = "";
                    for (int i = 0; i < activas.size(); i++)
                        lista += (i + 1) + "️⃣ " + activas.get(i).getNombre() + "\n";
                    return "📊 Generar reporte\n\nSelecciona una campaña:\n\n" + lista + "0️⃣ Reporte general";
                case "3":*/
                   /* estadoUsuario.put(numero, "LANDING");
                    List<Campania> activasLanding = campaniaService.obtenerActivas();
                    if (activasLanding.isEmpty()) return "❌ No tienes campañas activas.";
                    String listaLanding = "";
                    for (int i = 0; i < activasLanding.size(); i++)
                        listaLanding += (i + 1) + "️⃣ " + activasLanding.get(i).getNombre() + "\n";
                    return "🚀 Crear landing\n\nSelecciona campaña:\n\n" + listaLanding;
            }*/



        }

        if ("ACTIVAR".equals(estado)) {
            List<Campania> inactivas = campaniaService.obtenerInactivas();
            boolean encontrada = false;

            for (Campania c : inactivas) {
                if (c.getNombre().equalsIgnoreCase(mensaje)) {
                    // campaniaService.activar(c.getNombre());
                    estadoUsuario.remove(numero);
                    return "✅ La campaña \"" + c.getNombre() + "\" ha sido activada con éxito.";
                }
            }

            return "❌ No encontré ninguna campaña inactiva con el nombre: " + mensaje +
                    "\n\nPor favor, escribe el nombre exactamente como aparece en la lista o escribe *cancelar*.";
        }

        if ("DESACTIVAR".equals(estado)) {
            List<Campania> activas = campaniaService.obtenerActivas();
            for (Campania c : activas) {
                if (c.getNombre().equalsIgnoreCase(mensaje)) {
                    // campaniaService.desactivar(c.getNombre());
                    estadoUsuario.remove(numero);
                    return "🔴 La campaña \"" + c.getNombre() + "\" ha sido desactivada.";
                }
            }
            return "❌ No encontré ninguna campaña activa con ese nombre.";
        }

        if ("CONFIRMAR_AUDIO".equals(estado)) {
            if ("1".equals(mensaje)) {
                estadoUsuario.put(numero, "PROCESANDO");
                campaniaService.crear(
                        nombreCampania.get(numero),
                        duracionCampania.get(numero),
                        mensajeCampania.get(numero),
                        null,
                        numero
                );
                estadoUsuario.remove(numero);
                return "✅ ¡Excelente! La campaña ha sido creada con éxito. Ya puedes verla en tu panel.";

            } else if ("3".equals(mensaje)) {
                estadoUsuario.put(numero, "EDITANDO_AUDIO");
                return "✏️ Entendido, ¿qué cambios deseas hacer? \n\n*Escríbeme los ajustes* (ej: 'Que dure 15 días y cambia el nombre a Promo Gasolina').";

            } else {
                estadoUsuario.remove(numero);
                return "Entendido, he cancelado la solicitud. ¿En qué más puedo ayudarte? 😊";
            }
        }
        if ("EDITANDO_AUDIO".equals(estado)) {
            String transcripcionOriginal = transcripcionesTemporales.get(numero);

            String promptEdicion = "El usuario grabó un audio que decía: \"" + transcripcionOriginal + "\".\n" +
                    "Pero ahora pide estos cambios: \"" + mensaje + "\".\n" +
                    "Basado en esta corrección, responde ÚNICAMENTE un JSON con los nuevos datos:\n" +
                    "{\"nombre\":\"...\",\"duracion\":30,\"tema\":\"...\",\"esValido\":true, \"transcripcion\":\"(aquí el resumen de la nueva versión)\"}";

            try {
                String respuestaJson = geminiService.analizarTexto(promptEdicion);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode datos = mapper.readTree(respuestaJson);

                nombreCampania.put(numero, datos.path("nombre").asText());
                duracionCampania.put(numero, datos.path("duracion").asInt());
                mensajeCampania.put(numero, datos.path("tema").asText());
                String nuevaTranscripcion = datos.path("transcripcion").asText();

                estadoUsuario.put(numero, "CONFIRMAR_AUDIO");

                return "✅ *¡Mensaje de voz modificado!*\n" +
                        "--------------------------------\n" +
                        "🎙️ *Nueva versión:* \"" + nuevaTranscripcion + "\"\n\n" +
                        "🏷️ *Nombre:* " + nombreCampania.get(numero) + "\n" +
                        "📅 *Duración:* " + duracionCampania.get(numero) + " días\n" +
                        "💡 *Tema:* " + mensajeCampania.get(numero) + "\n" +
                        "--------------------------------\n" +
                        "¿Ahora sí es correcto?\n1️⃣ Sí, crear campaña\n2️⃣ No, cancelar\n3️⃣ Volver a modificar";

            } catch (Exception e) {
                return "⚠️ Hubo un error al procesar tus cambios. Por favor, intenta ser más específico.";
            }
        }

        if ("CONFIRMAR".equals(estado)) {
            if (mensaje.equals("1")) {
                String texto = mensajeCampania.get(numero);
                botService.enviarMensaje("5580323498", texto);
                estadoUsuario.remove(numero);
                return "📲 Mensaje de prueba enviado al número 5580323498.";
            } else if (mensaje.equals("2")) {
                estadoUsuario.put(numero, "MODIFICAR");
                return "¿Qué deseas modificar?\n1 Nombre\n2 Duración\n3 Mensaje";
            } else {
                estadoUsuario.remove(numero);
                return "Operación cancelada.";
            }
        }
        if ("MODIFICAR".equals(estado)) {
            if (mensaje.equals("1")) {
                estadoUsuario.put(numero, "EDITAR_NOMBRE");
                return "Escribe el nuevo nombre de la campaña.";
            } else if (mensaje.equals("2")) {
                estadoUsuario.put(numero, "EDITAR_DURACION");
                return "Escribe la nueva duración en días.";
            } else if (mensaje.equals("3")) {
                estadoUsuario.put(numero, "EDITAR_MENSAJE");
                return "Escribe el nuevo mensaje de la campaña.";
            }
        }

        if ("EDITAR_NOMBRE".equals(estado)) {
            nombreCampania.put(numero, mensaje);
            estadoUsuario.put(numero, "CONFIRMAR");
            return "Nombre actualizado.\n\n¿Enviar prueba?\n1 Sí\n2 Modificar otra cosa";
        }
        if ("EDITAR_DURACION".equals(estado)) {
            try {
                int duracion = parseDuracionADias(mensaje);
                //int dias = Integer.parseInt(mensaje);
                duracionCampania.put(numero, duracion);
                estadoUsuario.put(numero, "CONFIRMAR");
                return "Duración actualizada.\n\n¿Enviar prueba?\n1 Sí\n2 Modificar otra cosa";
            } catch (Exception e) {
                return "Escribe un número válido.";
            }
        }
        if ("EDITAR_MENSAJE".equals(estado)) {
            mensajeCampania.put(numero, mensaje);
            estadoUsuario.put(numero, "CONFIRMAR");
            return "Mensaje actualizado.\n\n¿Enviar prueba?\n1 Sí\n2 Modificar otra cosa";
        }

        if ("REPORTE".equals(estado)) {
            List<Campania> activas = campaniaService.obtenerActivas();
            if (activas.isEmpty()) {
                estadoUsuario.remove(numero);
                return "📊 No tienes campañas activas.";
            }

            if (mensaje.equals("0") || mensaje.contains("general")) {
                estadoUsuario.remove(numero);
                return "📊 Reporte general\n\n🔗 Descargar: https://gaffe-skeptic-hangnail.ngrok-free.dev/reporte/general";
            }

            try {
                int opcion = Integer.parseInt(mensaje);
                if (opcion > 0 && opcion <= activas.size()) {
                    Campania seleccionada = activas.get(opcion - 1);
                    estadoUsuario.remove(numero);
                    return "📊 Reporte generado\n\n📢 Campaña: " + seleccionada.getNombre() +
                            "\n\n🔗 Descargar: https://gaffe-skeptic-hangnail.ngrok-free.dev/reporte?campania=" + seleccionada.getNombre();
                }
            } catch (Exception ignored) {
            }

            String lista = "";
            for (int i = 0; i < activas.size(); i++) lista += (i + 1) + "️⃣ " + activas.get(i).getNombre() + "\n";
            return "📊 Generar reporte\n\nSelecciona una campaña:\n\n" + lista + "0️⃣ Reporte general";
        }
        if ("LANDING".equals(estado)) {
            List<Campania> activas = campaniaService.obtenerActivas();

            if (activas.isEmpty()) {
                estadoUsuario.remove(numero);
                return "❌ No tienes campañas activas.";
            }

            try {
                int opcion = Integer.parseInt(mensaje);

                if (opcion > 0 && opcion <= activas.size()) {

                    Campania seleccionada = activas.get(opcion - 1);

                    estadoUsuario.put(numero, "PROCESANDO");
                    botService.enviarMensaje(numero, "⏳ Generando tu landing, espera unos segundos...");

                    try {
                        String link = botService.crearLanding(seleccionada.getNombre());

                        estadoUsuario.remove(numero);

                        return "🚀 Landing generada\n\n📢 Campaña: " + seleccionada.getNombre() +
                                "\n\n🔗 Ver aquí:\n" + link;

                    } catch (Exception e) {
                        estadoUsuario.remove(numero);
                        return "❌ Error al generar la landing. Intenta nuevamente.";
                    }
                }

            } catch (Exception ignored) {
            }

            String lista = "";
            for (int i = 0; i < activas.size(); i++) {
                lista += (i + 1) + "️⃣ " + activas.get(i).getNombre() + "\n";
            }

            return "🚀 Crear landing\n\nSelecciona campaña:\n\n" + lista;
        }

        if (mensaje.equals("hola")) {
            List<Campania> activas = campaniaService.obtenerActivas();
            List<Campania> inactivas = campaniaService.obtenerInactivas();

            String listaActivas = activas.isEmpty() ? "No hay campañas activas" : "";
            for (int i = 0; i < activas.size(); i++) {
                if (!listaActivas.isEmpty()) listaActivas += ", ";
                listaActivas += activas.get(i).getNombre();
            }

            String listaInactivas = inactivas.isEmpty() ? "No hay campañas inactivas" : "";
            for (int i = 0; i < inactivas.size(); i++) {
                if (!listaInactivas.isEmpty()) listaInactivas += ", ";
                listaInactivas += inactivas.get(i).getNombre();
            }

          /*  return "Hola soy Efecampañas\n\n🟢 Campañas ACTIVAS:\n" + listaActivas +
                    "\n\n🔴 Campañas INACTIVAS:\n" + listaInactivas +
                    "\n\nSelecciona una opción:\n\n" +
                    "1️⃣ Crear campaña\n" +
                    "2️⃣ Activar campaña\n" +
                    "3️⃣ Desactivar campaña\n" +
                    "4️⃣ Descargar reporte de campaña\n" +
                    "5️⃣ Hacer landing de campaña\n";*/
            return "Hola soy Efecampañas\n\n🟢 Campañas ACTIVAS:\n" + listaActivas +
                    "\n\n🔴 Campañas INACTIVAS:\n" + listaInactivas +
                    "\n\nSelecciona una opción:\n\n" +
                    "1️⃣ Crear campaña\n" +
                    "2️⃣ Descargar reporte de campaña\n" +
                    "3️⃣ Hacer landing de campaña\n";
        }

        String infoPDF = buscarEnPDF(mensaje);
        if (infoPDF != null) return generarRespuestaIA(mensaje, infoPDF);
        String infoWeb = buscarEnWeb(mensaje);
        if (infoWeb != null) return generarRespuestaIA(mensaje, infoWeb);

        String ia = llamarGemini(
                "Responde SIEMPRE en español.\n" +
                        "Eres asistente de Efectivale.\n" +
                        "Solo puedes ayudar con vales, campañas, gasolina y despensa. " +
                        "Si el usuario pregunta otra cosa, responde educadamente que no puedes ayudar.\n\nUsuario: " + mensaje
        );
        if ("AUTENTICADO".equals(estado) && mensaje.equals("hola")) {
            return obtenerMenuPrincipal();
        }
        if (ia == null || ia.trim().isEmpty()) {
            return "No entendí tu mensaje 😅\nPuedes escribir *hola* para ver el menú o preguntarme sobre:\n- gasolina\n- despensa\n- tarjetas\n";
        }
        return ia;
    }

    private String generarRespuestaIA(String mensaje, String contexto) {

        return llamarGemini(
                "Responde como un experto de Efectivale.\n" +
                        "Solo usa la información del contexto.\n" +
                        "Si no hay suficiente información, responde de forma general.\n\n" +
                        "Contexto:\n" + contexto + "\n\n" +
                        "Pregunta:\n" + mensaje
        );
    }

    private String llamarGemini(String mensaje) {

        try {

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> body = new HashMap<>();

            Map<String, Object> part = new HashMap<>();
            part.put("text", mensaje);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Arrays.asList(part));

            body.put("contents", Arrays.asList(content));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map respBody = response.getBody();

            List candidates = (List) respBody.get("candidates");

            Map first = (Map) candidates.get(0);

            Map contentResp = (Map) first.get("content");

            List parts = (List) contentResp.get("parts");

            Map textPart = (Map) parts.get(0);

            return textPart.get("text").toString();

        } catch (Exception e) {

            e.printStackTrace();

            return "❌ Error al generar respuesta con IA.";
        }
    }

    private String buscarEnPDF(String mensaje) {

        String contenido = pdfService.getContenido();

        if (contenido == null) return null;

        if (contenido.toLowerCase().contains(mensaje.toLowerCase())) {

            int index = contenido.toLowerCase().indexOf(mensaje.toLowerCase());

            return contenido.substring(index, Math.min(index + 400, contenido.length()));
        }

        return null;
    }

    private String buscarEnWeb(String mensaje) {

        if (mensaje.contains("efectivale") || mensaje.contains("gasolina")) {

            return "Efectivale es una empresa que ofrece soluciones de vales de gasolina, despensa y beneficios para empresas.";
        }

        return null;
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumenCampanias() {

        List<Campania> activas = campaniaService.obtenerActivas();

        Map<String, Object> data = new HashMap<>();

        data.put("total", activas.size());
        data.put("campanias", activas);

        return data;
    }

    private String obtenerCampaniasActivas() {

        List<Campania> activas = campaniaService.obtenerActivas();

        if (activas.isEmpty()) {
            return "❌ No hay campañas activas.";
        }

        String lista = activas.stream()
                .map(Campania::getNombre)
                .reduce((a, b) -> a + "\n- " + b)
                .orElse("");

        return "🟢 Campañas ACTIVAS:\n\n- " + lista +
                "\n\n👉 Escribe el nombre de la campaña:";
    }

    private String obtenerCampaniasInactivas() {

        List<Campania> inactivas = campaniaService.obtenerInactivas();

        String listaInactivas = inactivas.isEmpty()
                ? "No hay campañas inactivas"
                : inactivas.stream()
                .map(Campania::getNombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return "🔴 Campañas Inactivas:\n" + listaInactivas +
                "\n\n👉 Escribe el nombre de la campaña:";
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> descargarPdf() {

        List<Campania> campanias = campaniaService.obtenerTodas();

        byte[] pdf = reporteService.generarPdf(campanias);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=reporte_campanias.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }

    private int parseDuracionADias(String texto) {
        texto = texto.toLowerCase().replaceAll("-", " ").trim();
        int dias = 0;

        Map<String, Integer> numeros = new HashMap<>();
        numeros.put("uno", 1);
        numeros.put("dos", 2);
        numeros.put("tres", 3);
        numeros.put("cuatro", 4);
        numeros.put("cinco", 5);
        numeros.put("seis", 6);
        numeros.put("siete", 7);
        numeros.put("ocho", 8);
        numeros.put("nueve", 9);
        numeros.put("diez", 10);

        Pattern p = Pattern.compile("(\\d+|uno|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez)\\s*(año|años|mes|meses|día|días|hora|horas)?");
        Matcher m = p.matcher(texto);

        while (m.find()) {
            String numStr = m.group(1);
            String unidad = m.group(2);

            int numero = numeros.containsKey(numStr) ? numeros.get(numStr) : -1;
            if (numero == -1) {
                try {
                    numero = Integer.parseInt(numStr);
                } catch (Exception e) {
                    numero = 0;
                }
            }

            if (unidad == null) unidad = "días";

            switch (unidad) {
                case "año":
                case "años":
                    dias += numero * 365;
                    break;
                case "mes":
                case "meses":
                    dias += numero * 30;
                    break;
                case "día":
                case "días":
                    dias += numero;
                    break;
                case "hora":
                case "horas":
                    dias += (numero > 0 ? 1 : 0);
                    break;
            }
        }

        return dias > 0 ? dias : 1;
    }

    public String procesarAudio(String numero, String mediaId) {
        try {
            byte[] audioData = whatsAppService.descargarMedia(mediaId);

            if (audioData == null) {
                return "❌ Hubo un problema al obtener el audio. Por favor, intenta de nuevo.";
            }
            String prompt = "Eres un asistente de Efectivale. Analiza el audio:\n" +
                    "1. Transcribe LITERALMENTE lo que dice el usuario en un campo 'transcripcion'.\n" +
                    "2. Si es una campaña, responde JSON: {\"nombre\":\"...\",\"duracion\":30,\"tema\":\"...\",\"esValido\":true, \"transcripcion\":\"...\"}\n" +
                    "3. Si es otro tema, responde JSON: {\"esValido\":false, \"transcripcion\":\"...\"}";

            String respuestaJson = geminiService.analizarAudio(audioData, prompt);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode datos = mapper.readTree(respuestaJson);

            String textoTranscrito = datos.path("transcripcion").asText("Audio procesado");
            transcripcionesTemporales.put(numero, "🎙️ " + textoTranscrito);

            if (!datos.path("esValido").asBoolean(false)) {
                estadoUsuario.remove(numero);

                return "¡Hola! 🎙️ Te escucho, pero por ahora solo estoy diseñado para ayudarte a crear *campañas de beneficios* (como vales de gasolina o despensa).\n\n" +
                        "No puedo ayudarte con pedidos de comida u otros temas, pero si gustas crear una campaña, ¡puedes decírmelo y con gusto te ayudo! 😊";
            }

            nombreCampania.put(numero, datos.path("nombre").asText("Campaña sin nombre"));
            duracionCampania.put(numero, datos.path("duracion").asInt(30));
            mensajeCampania.put(numero, datos.path("tema").asText("Sin descripción"));

            estadoUsuario.put(numero, "CONFIRMAR_AUDIO");

            return "🎙️ *He analizado tu nota de voz*:\n\n" +
                    "🏷️ *Campaña:* " + nombreCampania.get(numero) + "\n" +
                    "📅 *Duración:* " + duracionCampania.get(numero) + " días\n" +
                    "📝 *Tema:* " + mensajeCampania.get(numero) + "\n\n" +
                    "¿Qué deseas hacer?\n" +
                    "1️⃣ Sí, crear campaña\n" +
                    "2️⃣ No, cancelar\n" +
                    "3️⃣ Modificar datos (Escríbeme los cambios)";

        } catch (Exception e) {
            System.err.println("Error en procesarAudio: " + e.getMessage());
            return "⚠️ Entendí que enviaste un audio, pero no pude procesar los datos automáticamente. " +
                    "Por favor, intenta describirme la campaña por texto.";
        }
    }

    public String getUltimaTranscripcion(String numero) {
        return transcripcionesTemporales.getOrDefault(numero, "[Nota de voz]");
    }

    private String obtenerMenuPrincipal() {
        List<Campania> activas = campaniaService.obtenerActivas();
        List<Campania> inactivas = campaniaService.obtenerInactivas();

        String listaActivas = activas.isEmpty() ? "No hay" : "";
        for (int i = 0; i < activas.size(); i++) {
            listaActivas += (i > 0 ? ", " : "") + activas.get(i).getNombre();
        }

        String listaInactivas = inactivas.isEmpty() ? "No hay" : "";
        for (int i = 0; i < inactivas.size(); i++) {
            listaInactivas += (i > 0 ? ", " : "") + inactivas.get(i).getNombre();
        }

        return "Hola soy Efecampañas\n\n" +
                "🟢 Campañas ACTIVAS: " + listaActivas + "\n" +
                "🔴 Campañas INACTIVAS: " + listaInactivas + "\n\n" +
                "Selecciona una opción:\n" +
                "1️⃣ Crear campaña\n" +
                "2️⃣ Descargar reporte de campaña\n" +
                "3️⃣ Hacer landing de campaña";
    }

    @PostMapping("/enviar-masivo")
    public ResponseEntity<?> enviarCampaniaMasiva(@RequestBody EnvioMasivoDTO datos) {
        try {
            whatsAppService.procesarEnvioMasivo(datos);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "El proceso de envío masivo ha iniciado");
            respuesta.put("total", datos.getDestinatarios().size());

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    public List<Campania> listarCampanasActivas() {
        return campaniaRepository.findByEstadoIgnoreCase("ACTIVA");
    }

    @GetMapping("/interesados")
    public ResponseEntity<List<RespuestaCampania>> obtenerInteresados() {
        return ResponseEntity.ok(respuestaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaRespuesta")));
    }

    @GetMapping("/estado-general")
    public ResponseEntity<List<Map<String, Object>>> obtenerEstadoGeneral() {
        List<Map<String, Object>> reporte = whatsAppService.obtenerReporteSeguimiento();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/atender")
    public ResponseEntity<Void> registrarGanador(@RequestParam("data") String data) {
        try {
            String[] partes = data.split("&asesor=");
            String telCliente = partes[0];
            String nombreAsesor = partes[1];

            System.out.println("==============================================");
            System.out.println("🏆 ¡TENEMOS UN GANADOR!");
            System.out.println("👤 ASESOR: " + nombreAsesor);
            System.out.println("📱 CLIENTE: " + telCliente);
            System.out.println("⏰ HORA: " + java.time.LocalDateTime.now());
            System.out.println("==============================================");

            String urlWhatsapp = "https://wa.me/" + telCliente;
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(urlWhatsapp))
                    .build();

        } catch (Exception e) {
            System.err.println("❌ Error procesando el clic: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}