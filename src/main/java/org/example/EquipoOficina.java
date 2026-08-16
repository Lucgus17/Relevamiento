package org.example;

public class EquipoOficina {

    private String tipo;
    private String numeroSerie;
    private String nombre;
    private String ip;

    public EquipoOficina(String tipo, String numeroSerie, String nombre) {
        this(tipo, numeroSerie, nombre, null);
    }

    public EquipoOficina(String tipo, String numeroSerie, String nombre, String ip) {
        this.tipo = tipo;
        this.numeroSerie = numeroSerie;
        this.nombre = nombre;
        this.ip = ip;
    }

    public String getTipo() { return tipo; }
    public String getNumeroSerie() { return numeroSerie; }
    public String getNombre() { return nombre; }
    public String getIp() { return ip; }
}