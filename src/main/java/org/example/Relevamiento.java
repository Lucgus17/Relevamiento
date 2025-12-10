package org.example;

import java.util.*;

public class Relevamiento {

    private final List<String> numeroSerialEsperado = new ArrayList<>();
    private final Set<String> numeroSerialEncontrado = new LinkedHashSet<>();
    private final Set<String> numeroSerialSobrante = new LinkedHashSet<>();

    private String normalize(String s) {
        if (s == null) return null;
        return s.trim().toUpperCase();
    }

    // Agrega un serial directamente a la lista de sobrantes
    public void agregarSobrante(String serial) {
        if (serial == null || serial.isEmpty()) return;
        String n = serial.trim().toUpperCase();
        if (!numeroSerialSobrante.contains(n) && !numeroSerialEncontrado.contains(n)) {
            numeroSerialSobrante.add(n);
        }
    }


    public void cargarSeriales(List<String> seriales) {
        numeroSerialEsperado.clear();
        numeroSerialEncontrado.clear();
        numeroSerialSobrante.clear();

        if (seriales == null) return;

        for (String s : seriales) {
            String n = normalize(s);
            if (n != null && !n.isEmpty() && !numeroSerialEsperado.contains(n)) {
                numeroSerialEsperado.add(n);
            }
        }
    }

    // -------- Procesar un serial ingresado, devuelve sugerencia si aplica --------
    public String procesarInputConSugerencia(String rawSerial) {
        if (rawSerial == null) return null;
        String serial = normalize(rawSerial);
        if (serial.isEmpty()) return null;

        // Ya está en encontrados
        if (numeroSerialEncontrado.contains(serial)) return null;

        // Coincidencia exacta
        for (int i = 0; i < numeroSerialEsperado.size(); i++) {
            if (numeroSerialEsperado.get(i).equals(serial)) {
                numeroSerialEsperado.remove(i);
                numeroSerialEncontrado.add(serial);
                return null;
            }
        }

        // Buscar sugerencia de parecido
        String sugerido = buscarSerialParecido(serial);
        if (sugerido != null) return sugerido;

        // Si no hay sugerencia, lo agregamos a sobrantes
        if (!numeroSerialSobrante.contains(serial) && !numeroSerialEncontrado.contains(serial)) {
            numeroSerialSobrante.add(serial);
        }
        return null;
    }

    private String buscarSerialParecido(String serialIngresado) {
        for (String esperado : numeroSerialEsperado) {
            int dif = distanciaLevenshtein(serialIngresado, esperado);
            if (dif <= 2) return esperado;
        }
        return null;
    }

    // Distancia de Levenshtein para medir similitud
    private int distanciaLevenshtein(String a, String b) {
        if (a == null || b == null) return Integer.MAX_VALUE;
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
        Map<String, Long> conteos = new HashMap<>();
        conteos.put("esperados", (long) numeroSerialEsperado.size());
        conteos.put("encontrados", (long) numeroSerialEncontrado.size());
        conteos.put("sobrantes", (long) numeroSerialSobrante.size());
        return conteos;
    }

    public List<String> getNumeroSerialEsperado() { return numeroSerialEsperado; }
    public List<String> getNumeroSerialEncontrado() { return numeroSerialEncontrado.stream().toList(); }
    public List<String> getNumeroSerialSobrante() { return numeroSerialSobrante.stream().toList(); }

}
