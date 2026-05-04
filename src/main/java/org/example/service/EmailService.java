package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envía el token de seguridad al correo del personal autorizado.
     * @param destinatario Correo del usuario.
     * @param token Código de 6 dígitos generado.
     */
    public void enviarToken(String destinatario, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom("no-reply@efectivale.com.mx"); // Tu correo configurado
            message.setTo(destinatario);
            message.setSubject("🔐 Código de Acceso - Efecampañas");

            String cuerpoMensaje = "Hola,\n\n" +
                    "Se ha solicitado un acceso al bot de Efecampañas desde tu número de WhatsApp.\n\n" +
                    "Tu código de verificación es: " + token + "\n\n" +
                    "⚠️ NOTA IMPORTANTE:\n" +
                    "- Este código vence en 24 horas.\n" +
                    "- Solo tienes 1 intento para ingresarlo correctamente.\n" +
                    "- Si fallas, tu acceso será bloqueado por seguridad.\n\n" +
                    "Si no solicitaste este código, por favor ignora este correo.\n\n" +
                    "Saludos,\nEquipo de TI Efectivale.";

            message.setText(cuerpoMensaje);

            mailSender.send(message);
            System.out.println("✅ Correo enviado con éxito a: " + destinatario);

        } catch (Exception e) {
            System.err.println("❌ Error al enviar el correo: " + e.getMessage());
        }
    }
}