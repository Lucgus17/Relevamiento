package org.example;

import java.util.ArrayList;
import java.util.List;


public class Oficina {

    private String nombre;
    private List<Empleado> empleados = new ArrayList<>();
    private List<EquipoOficina> equiposOficina = new ArrayList<>();
    public void agregarEmpleado(Empleado e) { empleados.add(e); }
    public void agregarEquipoOficina(EquipoOficina e) { equiposOficina.add(e); }

    public Oficina() {
    }

    public Oficina(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Empleado> getEmpleados() {
        return empleados;
    }

    public void setEmpleados(List<Empleado> empleados) {
        this.empleados = empleados;
    }

    public List<EquipoOficina> getEquiposOficina() {
        return equiposOficina;
    }

    public void setEquiposOficina(List<EquipoOficina> equiposOficina) {
        this.equiposOficina = equiposOficina;
    }
}