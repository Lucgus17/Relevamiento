package org.example;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;


public class Relevamiento {

    // Internamente usamos lista para esperado (orden) y sets ordenados para encontrado/sobrante (evitan duplicados)
    private final List<String> numeroSerialEsperado = new ArrayList<>();
    private final Set<String> numeroSerialEncontrado = new LinkedHashSet<>();
    private final Set<String> numeroSerialSobrante = new LinkedHashSet<>();


    /** Normaliza un serial: trim y pasar a mayúsculas (para comparaciones consistentes). */
    private String normalize(String s) {
        if (s == null) return null;
        return s.trim().toUpperCase();
    }

    /** Carga los seriales esperados desde una lista (por ejemplo leída del Excel). */
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

    /**
     * Procesa un serial ingresado por el usuario:
     * - si existe en esperado => lo mueve a encontrado (y lo elimina de esperado)
     * - si NO existe en esperado => lo agrega a sobrante (si no está ya ahí)
     */
    public void procesarInput(String rawSerial) {
        if (rawSerial == null) return;
        String serial = normalize(rawSerial);
        if (serial.isEmpty()) return;

        // Si ya está en encontrados, no hacemos nada
        if (numeroSerialEncontrado.contains(serial)) return;

        // Buscamos en esperado (case-insensitive gracias a normalize)
        boolean estabaEnEsperado = false;
        for (int i = 0; i < numeroSerialEsperado.size(); i++) {
            if (numeroSerialEsperado.get(i).equals(serial)) {
                // mover a encontrados
                numeroSerialEsperado.remove(i);
                numeroSerialEncontrado.add(serial);
                estabaEnEsperado = true;
                break;
            }
        }

        // Si no estaba en esperado, lo agregamos a sobrantes (si no estaba ya)
        if (!estabaEnEsperado) {
            if (!numeroSerialSobrante.contains(serial) && !numeroSerialEncontrado.contains(serial)) {
                numeroSerialSobrante.add(serial);
            }
        }
    }

    public Map<String, Long> contarNumerosSeriales() {
        Map<String, Long> conteos = new HashMap<>();

        conteos.put("esperados", (long) numeroSerialEsperado.size());
        conteos.put("encontrados", (long) numeroSerialEncontrado.size());
        conteos.put("sobrantes", (long) numeroSerialSobrante.size());

        return conteos;
    }



    /** Métodos auxiliares para obtener datos (para mostrar en la vista) */
    public List<String> getNumeroSerialEsperado() {
        return numeroSerialEsperado;
    }

    public List<String> getNumeroSerialEncontrado() {
        return numeroSerialEncontrado.stream().toList();
    }

    public List<String> getNumeroSerialSobrante() {
        return numeroSerialSobrante.stream().toList();
    }


    /** Utilidades: contadores si los necesitás */
    public int getCountEsperado() { return numeroSerialEsperado.size(); }
    public int getCountEncontrado() { return numeroSerialEncontrado.size(); }
    public int getCountSobrante() { return numeroSerialSobrante.size(); }
}
