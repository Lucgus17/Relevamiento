package org.example;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
public class HistorialOficinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private LocalDateTime fecha;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "historial_oficina_id")
    @OrderColumn(name = "orden")
    private List<OficinaEntity> oficinas = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String n) {
        this.nombre = n;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime f) {
        this.fecha = f;
    }

    public String getFechaFormateada() {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public List<OficinaEntity> getOficinas() {
        return oficinas;
    }

    public int getTotalEmpleados() {
        return oficinas.stream().mapToInt(o -> o.getEmpleados().size()).sum();
    }

    public int getTotalEquiposOficina() {
        return oficinas.stream().mapToInt(o -> o.getEquiposOficina().size()).sum();
    }
}