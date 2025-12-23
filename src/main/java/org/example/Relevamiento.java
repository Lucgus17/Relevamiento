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

    private String normalizeStrong(String s) {
        if (s == null) return "";
        return s.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    // ===================== MÉTODOS NUEVOS CLAVE =====================

    public void marcarComoEncontrado(String serial) {
        String n = normalize(serial);
        if (n == null || n.isEmpty()) return;

        numeroSerialEsperado.removeIf(s -> s.equals(n));

        if (!numeroSerialEncontrado.contains(n)) {
            numeroSerialEncontrado.add(0, n);
        }

        numeroSerialSobrante.remove(n);
    }

    public void agregarSobrante(String serial) {
        String n = normalize(serial);
        if (n == null || n.isEmpty()) return;

        if (!numeroSerialSobrante.contains(n) && !numeroSerialEncontrado.contains(n)) {
            numeroSerialSobrante.add(0, n);
        }
    }

    // ===================== CARGA =====================

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

    // ===================== LÓGICA PRINCIPAL =====================

    public String procesarInputConSugerencia(String rawSerial) {
        if (rawSerial == null) return null;

        String serialNormal = normalize(rawSerial);
        if (serialNormal == null || serialNormal.isEmpty()) return null;

        // 1️⃣ Ya encontrado
        if (numeroSerialEncontrado.contains(serialNormal)) return null;

        // 2️⃣ Coincidencia exacta
        for (int i = 0; i < numeroSerialEsperado.size(); i++) {
            if (numeroSerialEsperado.get(i).equals(serialNormal)) {
                marcarComoEncontrado(serialNormal);
                return null;
            }
        }

        // 3️⃣ Normalización fuerte
        String serialFuerte = normalizeStrong(rawSerial);
        if (serialFuerte.isEmpty()) {
            agregarSobrante(serialNormal);
            return null;
        }

        // 4️⃣ Contención
        for (String esperado : numeroSerialEsperado) {
            String esperadoLimpio = normalizeStrong(esperado);
            if (serialFuerte.contains(esperadoLimpio) && !serialFuerte.equals(esperadoLimpio)) {
                return esperado;
            }
        }

        // 5️⃣ Levenshtein
        String sugerencia = buscarSerialParecido(serialFuerte);
        if (sugerencia != null) {
            return sugerencia;
        }

        // 6️⃣ Sobrante
        agregarSobrante(serialNormal);
        return null;
    }

    private String buscarSerialParecido(String serialIngresado) {
        String mejor = null;
        int menor = Integer.MAX_VALUE;

        for (String esperado : numeroSerialEsperado) {
            String e = normalizeStrong(esperado);

            if (serialIngresado.contains(e) || e.contains(serialIngresado)) continue;

            int d = distancia(serialIngresado, e);
            int len = Math.max(serialIngresado.length(), e.length());

            double umbral = len <= 6 ? 1 : len <= 12 ? Math.ceil(len * 0.10) : Math.ceil(len * 0.15);

            if (d <= umbral && d < menor) {
                menor = d;
                mejor = esperado;
            }
        }
        return mejor;
    }

    private int distancia(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? dp[i - 1][j - 1]
                        : 1 + Math.min(dp[i - 1][j - 1],
                        Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return dp[a.length()][b.length()];
    }

    // ===================== GETTERS (solo lectura) =====================

    public List<String> getNumeroSerialEsperado() {
        return numeroSerialEsperado;
    }

    public List<String> getNumeroSerialEncontrado() {
        return new ArrayList<>(numeroSerialEncontrado);
    }

    public List<String> getNumeroSerialSobrante() {
        return new ArrayList<>(numeroSerialSobrante);
    }

    public Map<String, Long> contarNumerosSeriales() {
        Map<String, Long> m = new HashMap<>();
        m.put("esperados", (long) numeroSerialEsperado.size());
        m.put("encontrados", (long) numeroSerialEncontrado.size());
        m.put("sobrantes", (long) numeroSerialSobrante.size());
        return m;
    }

    public void eliminar(String serial) {
        String n = normalize(serial);
        numeroSerialEncontrado.remove(n);
        numeroSerialSobrante.remove(n);
    }
}
