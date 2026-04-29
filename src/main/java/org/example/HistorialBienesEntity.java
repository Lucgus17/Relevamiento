package org.example;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "historial_bienes")
public class HistorialBienesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private LocalDateTime fecha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hb_esperados", joinColumns = @JoinColumn(name = "historial_id"))
    @OrderColumn(name = "orden")
    @Column(name = "serial")
    private List<String> esperados = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hb_encontrados", joinColumns = @JoinColumn(name = "historial_id"))
    @OrderColumn(name = "orden")
    @Column(name = "serial")
    private List<String> encontrados = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hb_sobrantes", joinColumns = @JoinColumn(name = "historial_id"))
    @OrderColumn(name = "orden")
    @Column(name = "serial")
    private List<String> sobrantes = new ArrayList<>();

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime f) { this.fecha = f; }
    public String getFechaFormateada() {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    public List<String> getEsperados() { return esperados; }
    public void setEsperados(List<String> e) { this.esperados = e; }
    public List<String> getEncontrados() { return encontrados; }
    public void setEncontrados(List<String> e) { this.encontrados = e; }
    public List<String> getSobrantes() { return sobrantes; }
    public void setSobrantes(List<String> e) { this.sobrantes = e; }
}