package org.example;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;

public class Empleado {

    private String nombre;
    private String cargo;
    private List<EquipoUsuario> equipos = new ArrayList<>();

    public Empleado(String nombre, String cargo) {
        this.nombre = nombre.trim();
        this.cargo = cargo;
    }

    public Empleado(String nombre) {
        this.nombre = nombre.trim();
    }

    public Empleado() {
        // Constructor vacío para crear empleados desde Excel
    }

    // GETTERS
    public String getNombre() {
        return nombre;
    }

    public String getCargo() {
        return cargo;
    }

    public List<EquipoUsuario> getEquipos() {
        return equipos;
    }

    // SETTERS (necesarios para el ExportService)
    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public void agregarEquipo(EquipoUsuario eq) {
        equipos.add(eq);
    }
}
=======
public class Empleado {
}
>>>>>>> e3e2929cf987aaf0588efea2f1e4c18f600e17e4
