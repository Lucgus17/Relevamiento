package org.example;

import java.util.*;

public class Relevamiento {

    private List<String> numeroSerialEsperado = new ArrayList<>();
    private List<String> numeroSerialEncontrado = new ArrayList<>();
    private List<String> numeroSerialSobrante = new ArrayList<>();

    // =====================================================================
    // CARGAR SERIALES DESDE EXCEL
    // =====================================================================
    public void cargarSeriales(List<String> seriales) {
        this.numeroSerialEsperado.clear();
        this.numeroSerialEsperado.addAll(seriales);
    }

    // =====================================================================
    // PROCESAR INPUT CON SUGERENCIA (LEVENSHTEIN)
    // =====================================================================
    public String procesarInputConSugerencia(String serialIngresado) {
        String serialNormalizado = serialIngresado.trim().toUpperCase();

        // 1. Verificar si está en esperados (match exacto)
        for (String esperado : numeroSerialEsperado) {
            if (esperado.trim().equalsIgnoreCase(serialNormalizado)) {
                marcarComoEncontrado(esperado);
                return null; // No hay sugerencia, ya se procesó
            }
        }

        // 2. Buscar similitud con Levenshtein
        String sugerencia = buscarSugerencia(serialNormalizado);

        if (sugerencia != null) {
            // Hay una sugerencia, retornarla para que el frontend la muestre
            return sugerencia;
        } else {
            // No hay sugerencia, agregar como sobrante
            agregarSobrante(serialNormalizado);
            return null;
        }
    }

    // =====================================================================
    // BUSCAR SUGERENCIA CON LEVENSHTEIN
    // =====================================================================
    private String buscarSugerencia(String serialIngresado) {
        int umbral = 3; // Máxima distancia permitida para sugerir
        String mejorMatch = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (String esperado : numeroSerialEsperado) {
            int distancia = calcularLevenshtein(
                    serialIngresado.toUpperCase(),
                    esperado.trim().toUpperCase()
            );

            if (distancia <= umbral && distancia < menorDistancia) {
                menorDistancia = distancia;
                mejorMatch = esperado;
            }
        }

        return mejorMatch;
    }

    // =====================================================================
    // ALGORITMO DE LEVENSHTEIN
    // =====================================================================
    private int calcularLevenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int costo = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + costo
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    // =====================================================================
    // MARCAR COMO ENCONTRADO
    // =====================================================================
    public void marcarComoEncontrado(String serial) {
        String serialNormalizado = serial.trim();

        // Remover de esperados
        numeroSerialEsperado.removeIf(s -> s.trim().equalsIgnoreCase(serialNormalizado));

        // Agregar a encontrados si no está ya
        boolean yaExiste = numeroSerialEncontrado.stream()
                .anyMatch(s -> s.trim().equalsIgnoreCase(serialNormalizado));

        if (!yaExiste) {
            numeroSerialEncontrado.add(serialNormalizado);
        }
    }

    // =====================================================================
    // AGREGAR SOBRANTE
    // =====================================================================
    public void agregarSobrante(String serial) {
        String serialNormalizado = serial.trim();

        boolean yaExiste = numeroSerialSobrante.stream()
                .anyMatch(s -> s.trim().equalsIgnoreCase(serialNormalizado));

        if (!yaExiste) {
            numeroSerialSobrante.add(serialNormalizado);
        }
    }

    // =====================================================================
    // ELIMINAR (CON LÓGICA MEJORADA - AGREGAR AL PRINCIPIO)
    // =====================================================================
    public void eliminar(String serial) {
        String serialNormalizado = serial.trim();

        // Verificar si está en ENCONTRADOS
        boolean estabEnEncontrados = numeroSerialEncontrado.removeIf(
                s -> s.trim().equalsIgnoreCase(serialNormalizado)
        );

        // Si estaba en encontrados, devolverlo al PRINCIPIO de esperados
        if (estabEnEncontrados) {
            boolean yaEstaEnEsperados = numeroSerialEsperado.stream()
                    .anyMatch(s -> s.trim().equalsIgnoreCase(serialNormalizado));

            if (!yaEstaEnEsperados) {
                // ⭐ AGREGAR AL PRINCIPIO en lugar de al final
                numeroSerialEsperado.add(0, serialNormalizado);
            }
            return; // Ya terminamos
        }

        // Si no estaba en encontrados, eliminar de sobrantes
        numeroSerialSobrante.removeIf(
                s -> s.trim().equalsIgnoreCase(serialNormalizado)
        );
    }

    // =====================================================================
    // CONTAR NÚMEROS DE SERIE
    // =====================================================================
    public Map<String, Integer> contarNumerosSeriales() {
        Map<String, Integer> conteos = new HashMap<>();
        conteos.put("esperados", numeroSerialEsperado.size());
        conteos.put("encontrados", numeroSerialEncontrado.size());
        conteos.put("sobrantes", numeroSerialSobrante.size());
        return conteos;
    }

    // =====================================================================
    // GETTERS
    // =====================================================================
    public List<String> getNumeroSerialEsperado() {
        return numeroSerialEsperado;
    }

    public List<String> getNumeroSerialEncontrado() {
        return numeroSerialEncontrado;
    }

    public List<String> getNumeroSerialSobrante() {
        return numeroSerialSobrante;
    }
}