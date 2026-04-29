package org.example;

import jakarta.persistence.*;

@Entity
@Table(name = "hist_equipo_usuario")
public class EquipoUsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;
    private String numeroSerie;
    private String nombre;

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public void setTipo(String t) { this.tipo = t; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String s) { this.numeroSerie = s; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
}
