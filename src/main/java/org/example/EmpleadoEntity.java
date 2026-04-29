package org.example;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hist_empleado")
public class EmpleadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String cargo;

    @Column(length = 2000)
    private String comentario;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "empleado_id")
    @OrderColumn(name = "orden")
    private List<EquipoUsuarioEntity> equipos = new ArrayList<>();

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getCargo() { return cargo; }
    public void setCargo(String c) { this.cargo = c; }
    public String getComentario() { return comentario; }
    public void setComentario(String c) { this.comentario = c; }
    public List<EquipoUsuarioEntity> getEquipos() { return equipos; }
}