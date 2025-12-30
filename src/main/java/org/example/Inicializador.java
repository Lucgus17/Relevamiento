package org.example;

import org.springframework.boot.SpringApplication;
import javax.swing.*;
import java.awt.Desktop;
import java.net.URI;

public class Inicializador {
    public static void main(String[] args) {
        // Configuramos la interfaz visual
        JFrame frame = new JFrame("Controlador del Servidor");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Centra la ventana

        JButton btnIniciar = new JButton("Iniciar Servidor y Abrir Navegador");

        btnIniciar.addActionListener(e -> {
            // 1. Deshabilitar el botón para evitar múltiples clics
            btnIniciar.setEnabled(false);
            btnIniciar.setText("Iniciando...");

            // 2. Iniciar Spring Boot en un nuevo hilo (Thread)
            // Esto evita que la ventana de Swing se bloquee mientras carga el servidor
            new Thread(() -> {
                try {
                    // Llamamos al main de tu aplicación Spring
                    SpringApplication.run(RelevamientoApplication.class);

                    // 3. Una vez iniciado, abrimos el navegador
                    abrirNavegador("http://localhost:8080");

                    SwingUtilities.invokeLater(() -> btnIniciar.setText("Servidor Online"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame, "Error al iniciar: " + ex.getMessage());
                        btnIniciar.setEnabled(true);
                    });
                }
            }).start();
        });

        frame.getContentPane().add(btnIniciar);
        frame.setVisible(true);
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
