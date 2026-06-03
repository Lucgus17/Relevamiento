package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import javax.swing.*;
import java.awt.Desktop;
import java.net.URI;

public class Inicializador {

    public static void main(String[] args) {
        // Ventana principal
        JFrame frame = new JFrame("Controlador del Servidor");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JButton btnIniciar = new JButton("Iniciar Servidor y Abrir Navegador");
        frame.getContentPane().add(btnIniciar);
        frame.setVisible(true);

        btnIniciar.addActionListener(e -> {
            btnIniciar.setEnabled(false);
            btnIniciar.setText("Iniciando...");

            new Thread(() -> {
                try {
                    // Inicia Spring Boot
                    SpringApplication app = new SpringApplication(RelevamientoApplication.class);

                    // Abrir navegador cuando Spring Boot esté listo
                    app.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> {
                        abrirNavegador("http://localhost:8080");
                        SwingUtilities.invokeLater(() -> btnIniciar.setText("Servidor Online"));
                    });

                    app.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame, "Error al iniciar: " + ex.getMessage());
                        btnIniciar.setEnabled(true);
                        btnIniciar.setText("Iniciar Servidor y Abrir Navegador");
                    });
                }
            }).start();
        });
    }

    private static void abrirNavegador(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            System.err.println("No se pudo abrir el navegador: " + e.getMessage());
        }
    }
}