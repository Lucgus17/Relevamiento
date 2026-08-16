package org.example;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class OficinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "oficina_entity_id")
    @OrderColumn(name = "orden")
    private List<EmpleadoEntity> empleados = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "oficina_entity_id")
    @OrderColumn(name = "orden")
    private List<EquipoOficinaEntity> equiposOficina = new ArrayList<>();

    public OficinaEntity() {}

    public OficinaEntity(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<EmpleadoEntity> getEmpleados() { return empleados; }
    public List<EquipoOficinaEntity> getEquiposOficina() { return equiposOficina; }
}