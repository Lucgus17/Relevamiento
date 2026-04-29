package org.example;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hist_oficina")
public class HistorialOficinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private LocalDateTime fecha;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "historial_oficina_id")
    @OrderColumn(name = "orden")
    private List<EmpleadoEntity> empleados = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "historial_oficina_id")
    @OrderColumn(name = "orden")
    private List<EquipoOficinaEntity> equiposOficina = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }   // ← nuevo
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime f) { this.fecha = f; }
    public String getFechaFormateada() {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    public List<EmpleadoEntity> getEmpleados() { return empleados; }
    public List<EquipoOficinaEntity> getEquiposOficina() { return equiposOficina; }
}