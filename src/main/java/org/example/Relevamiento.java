package org.example;

import java.util.*;

public class Relevamiento {

    private final List<String> numeroSerialEsperado = new ArrayList<>();
    private final List<String> numeroSerialEncontrado = new ArrayList<>();
    private final List<String> numeroSerialSobrante = new ArrayList<>();

    private String normalize(String s) {
        if (s == null) return null;
        return s.trim().toUpperCase();
    }

    public void agregarSobrante(String serial) {
        String n = normalize(serial);
        if (n == null || n.isEmpty()) return;

        if (!numeroSerialSobrante.contains(n) &&
                !numeroSerialEncontrado.contains(n)) {

            numeroSerialSobrante.add(0, n); // agregar arriba
        }
    }



    public void cargarSeriales(List<String> seriales) {
        numeroSerialEsperado.clear();
        numeroSerialEncontrado.clear();
        numeroSerialSobrante.clear();

        if (seriales == null) return;

        for (String s : seriales) {
            String n = normalize(s);
            if (n != null && !n.isEmpty()) {
                numeroSerialEsperado.add(n);
            }
        }
    }

    public String procesarInputConSugerencia(String rawSerial) {
        if (rawSerial == null) return null;

        // 🔥 limpieza TOTAL
        String serial = normalizeStrong(rawSerial);
        if (serial.isEmpty()) return null;

        // 1️⃣ Coincidencia exacta
        if (numeroSerialEncontrado.contains(serial)) return null;

        for (int i = 0; i < numeroSerialEsperado.size(); i++) {
            if (numeroSerialEsperado.get(i).equals(serial)) {
                numeroSerialEncontrado.add(0, serial);
                numeroSerialEsperado.remove(i);
                return null;
            }
        }

        // 2️⃣ Buscar si el serial esperado está contenido dentro del texto limpio
        for (String esperado : numeroSerialEsperado) {
            String esperadoLimpio = normalizeStrong(esperado);
            if (serial.contains(esperadoLimpio)) {
                return esperado;   // dispara modal
            }
        }

        // 3️⃣ Comparación por Levenshtein (diferencias pero similar)
        String sugerencia = buscarSerialParecido(serial);
        if (sugerencia != null) return sugerencia;

        // 4️⃣ Sobrante
        if (!numeroSerialSobrante.contains(serial)) {
            numeroSerialSobrante.add(0, serial);
        }

        return null;
    }



    private String buscarSerialParecido(String serialIngresado) {
        if (serialIngresado == null || serialIngresado.isEmpty()) return null;

        String ingresado = normalizeStrong(serialIngresado);

        for (String esperado : numeroSerialEsperado) {
            String esperadoLimpio = normalizeStrong(esperado);

            // Si está contenido directamente (caso ideal)
            if (ingresado.contains(esperadoLimpio)) {
                return esperado;
            }

            // Distancia Levenshtein
            int dif = distancia(ingresado, esperadoLimpio);
            int len = Math.max(ingresado.length(), esperadoLimpio.length());

            // ================================
            // 📌 Calcular umbral dinámico
            // ================================
            double umbral;

            if (len <= 6) {
                umbral = 1; // super estricto
            } else if (len <= 12) {
                umbral = Math.ceil(len * 0.10); // 10%
            } else {
                umbral = Math.ceil(len * 0.15); // 15%
            }

            // Comparar con umbral dinámico
            if (dif <= umbral) {
                return esperado;
            }
        }

        return null;
    }



    private int distancia(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1];
                else dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                        Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return dp[a.length()][b.length()];
    }

    public Map<String, Long> contarNumerosSeriales() {
        Map<String, Long> m = new HashMap<>();
        m.put("esperados", (long) numeroSerialEsperado.size());
        m.put("encontrados", (long) numeroSerialEncontrado.size());
        m.put("sobrantes", (long) numeroSerialSobrante.size());
        return m;
    }

    public List<String> getNumeroSerialEsperado() { return numeroSerialEsperado; }

    public List<String> getNumeroSerialEncontrado() {
        return new ArrayList<>(numeroSerialEncontrado); // ya no se invierte
    }

    public List<String> getNumeroSerialSobrante() {
        return new ArrayList<>(numeroSerialSobrante); // ya no se invierte
    }

    public void eliminar(String serial) {
        serial = normalize(serial);
        numeroSerialEncontrado.remove(serial);
        numeroSerialSobrante.remove(serial);
    }
    private String normalizeStrong(String s) { //Es para el reconocimiento de las palabras
        if (s == null) return "";
        return s
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", ""); // elimina TODO lo que no sea letra o número
    }


}
