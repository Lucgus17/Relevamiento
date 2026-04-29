package org.example;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void enviarExcel(String destinatario, String nombreRelevamiento,
                            byte[] excel, String nombreArchivo) throws MessagingException {
        if (mailSender == null) throw new IllegalStateException("El servicio de mail no está configurado.");

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setTo(destinatario);
        helper.setSubject(nombreRelevamiento);
        helper.setText(construirCuerpo(nombreRelevamiento, destinatario, nombreArchivo), true);
        helper.addAttachment(nombreArchivo, () -> new java.io.ByteArrayInputStream(excel));

        mailSender.send(mensaje);
    }

    private String construirCuerpo(String nombreRelevamiento, String destinatario, String nombreArchivo) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"));
        String usuario = destinatario.contains("@") ? destinatario.split("@")[0] : destinatario;

        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background:#f1f5f9;font-family:Segoe UI,Arial,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f1f5f9;padding:40px 0;'>"
                + "<tr><td align='center'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>"

                // HEADER
                + "<tr><td style='background:#1e293b;border-radius:16px 16px 0 0;padding:40px;text-align:center;'>"
                + "<div style='font-size:48px;margin-bottom:16px;'>📋</div>"
                + "<h1 style='margin:0 0 8px;color:#22c55e;font-size:22px;font-weight:700;'>Sistema de Relevamiento</h1>"
                + "<p style='margin:0;color:#64748b;font-size:13px;text-transform:uppercase;letter-spacing:1px;'>Justicia Córdoba</p>"
                + "</td></tr>"

                // CUERPO
                + "<tr><td style='background:#ffffff;padding:40px;'>"
                + "<p style='margin:0 0 6px;color:#64748b;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:0.8px;'>Hola,</p>"
                + "<h2 style='margin:0 0 24px;color:#0f172a;font-size:20px;font-weight:700;'>" + usuario + "</h2>"
                + "<p style='margin:0 0 24px;color:#475569;font-size:15px;line-height:1.7;'>"
                + "Se ha completado y adjunto el relevamiento indicado. Encontrarás el archivo Excel con todos los datos registrados durante la sesión."
                + "</p>"

                // CARD INFO
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;margin-bottom:28px;'>"
                + "<tr><td style='padding:20px 24px;'>"

                + "<table width='100%' cellpadding='0' cellspacing='0'>"
                + "<tr><td style='padding:8px 0;border-bottom:1px solid #e2e8f0;'>"
                + "<span style='color:#94a3b8;font-size:12px;font-weight:600;text-transform:uppercase;'>Relevamiento</span><br>"
                + "<span style='color:#0f172a;font-size:15px;font-weight:600;'>" + nombreRelevamiento + "</span>"
                + "</td></tr>"
                + "<tr><td style='padding:8px 0;border-bottom:1px solid #e2e8f0;'>"
                + "<span style='color:#94a3b8;font-size:12px;font-weight:600;text-transform:uppercase;'>Generado</span><br>"
                + "<span style='color:#0f172a;font-size:15px;font-weight:600;'>" + fecha + "</span>"
                + "</td></tr>"
                + "<tr><td style='padding:8px 0;'>"
                + "<span style='color:#94a3b8;font-size:12px;font-weight:600;text-transform:uppercase;'>Destinatario</span><br>"
                + "<span style='color:#0f172a;font-size:15px;font-weight:600;'>" + destinatario + "</span>"
                + "</td></tr>"
                + "</table>"

                + "</td></tr></table>"

                + "<p style='margin:0 0 24px;color:#64748b;font-size:13px;line-height:1.6;"
                + "background:#f8fafc;border-left:3px solid #22c55e;border-radius:4px;padding:12px 16px;'>"
                + "📎 El archivo <strong style='color:#0f172a;font-family:Courier New,monospace;'>" + nombreArchivo + "</strong>"
                + " se encuentra adjunto en este correo."
                + "</p>"

                + "<p style='margin:0;color:#94a3b8;font-size:13px;line-height:1.6;border-top:1px solid #f1f5f9;padding-top:20px;'>"
                + "Este correo fue generado automáticamente por el Sistema de Relevamiento. Por favor no respondas a este mensaje."
                + "</p>"
                + "</td></tr>"

                // FOOTER
                + "<tr><td style='background:#0f172a;border-radius:0 0 16px 16px;padding:24px 40px;text-align:center;'>"
                + "<p style='margin:0 0 6px;color:#475569;font-size:12px;'>Sistema de Relevamiento Avanzado</p>"
                + "<p style='margin:0;color:#334155;font-size:11px;'>Poder Judicial de la Provincia de Córdoba</p>"
                + "</td></tr>"

                + "</table></td></tr></table>"
                + "</body></html>";
    }

    public boolean estaConfigurado() {
        return mailSender != null;
    }
}