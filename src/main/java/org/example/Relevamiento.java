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

            numeroSerialSobrante.add(n);
        }
    }

    public void eliminarSobrante(String serial) {
        numeroSerialSobrante.removeIf(s -> s.equalsIgnoreCase(serial));
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

        String serial = normalize(rawSerial);
        if (serial.isEmpty()) return null;

        if (numeroSerialEncontrado.contains(serial)) return null;

        for (int i = 0; i < numeroSerialEsperado.size(); i++) {
            if (numeroSerialEsperado.get(i).equals(serial)) {

                numeroSerialEncontrado.add(0, serial); // ← mostrar arriba del todo
                numeroSerialEsperado.remove(i);

                return null;
            }
        }

        String sugerencia = buscarSerialParecido(serial);
        if (sugerencia != null) return sugerencia;

        if (!numeroSerialSobrante.contains(serial)) {
            numeroSerialSobrante.add(serial);
        }

        return null;
    }

    private String buscarSerialParecido(String serialIngresado) {
        for (String esperado : numeroSerialEsperado) {
            int dif = distancia( serialIngresado, esperado );
            if (dif <= 2) return esperado;
        }
        return null;
    }

    private int distancia(String a, String b) {
        int[][] dp = new int[a.length()+1][b.length()+1];

        for (int i=0;i<=a.length();i++) dp[i][0]=i;
        for (int j=0;j<=b.length();j++) dp[0][j]=j;

        for (int i=1;i<=a.length();i++){
            for(int j=1;j<=b.length();j++){
                if(a.charAt(i-1)==b.charAt(j-1)) dp[i][j]=dp[i-1][j-1];
                else dp[i][j]=1 + Math.min(dp[i-1][j-1],
                        Math.min(dp[i-1][j], dp[i][j-1]));
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
        List<String> lista = new ArrayList<>(numeroSerialEncontrado);
        Collections.reverse(lista);
        return lista;
    }

    public List<String> getNumeroSerialSobrante() { return numeroSerialSobrante; }

    public void eliminar(String serial) {
        serial = normalize(serial);
        numeroSerialEncontrado.remove(serial);
        numeroSerialSobrante.remove(serial);
    }

}
