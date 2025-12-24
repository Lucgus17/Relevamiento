package org.example;

public class EquipoOficina {

    private String tipo;
    private String numeroSerie;
    private String nombre; // opcional

    public EquipoOficina(String tipo, String numeroSerie, String nombre) {
        this.tipo = tipo;
        this.numeroSerie = numeroSerie;
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public String getNombre() {
        return nombre;
    }
}

