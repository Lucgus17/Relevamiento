package org.example;

import java.util.ArrayList;
import java.util.List;

public class RelevamientoOficina {

    private String nombre;
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<EquipoOficina> equiposOficina = new ArrayList<>();

    // ✅ CONSTRUCTOR
    public RelevamientoOficina(String nombre) {
        this.nombre = nombre;
    }

    // ✅ INICIAR / REINICIAR RELEVAMIENTO
    public void iniciar(String nombre, List<Empleado> empleadosIniciales) {
        this.nombre = nombre;
        empleados.clear();
        equiposOficina.clear();
        if (empleadosIniciales != null) {
            empleados.addAll(empleadosIniciales);
        }
    }

    public String getNombre() {
        return nombre;
    }

    public List<Empleado> getEmpleados() {
        return empleados;
    }

    public List<EquipoOficina> getEquiposOficina() {
        return equiposOficina;
    }

    // ===== Conteos Totales =====

    public int getTotalCPUs() {
        return (int) empleados.stream()
                .flatMap(e -> e.getEquipos().stream())
                .filter(eq -> "CPU".equalsIgnoreCase(eq.getTipo()))
                .count();
    }

    public int getTotalMonitores() {
        return (int) empleados.stream()
                .flatMap(e -> e.getEquipos().stream())
                .filter(eq -> "Monitor".equalsIgnoreCase(eq.getTipo()))
                .count();
    }

    public int getTotalTelefonos() {
        return (int) empleados.stream()
                .flatMap(e -> e.getEquipos().stream())
                .filter(eq -> "Teléfono IP".equalsIgnoreCase(eq.getTipo()))
                .count();
    }

    public int getTotalCamaras() {
        return (int) empleados.stream()
                .flatMap(e -> e.getEquipos().stream())
                .filter(eq -> "Cámara".equalsIgnoreCase(eq.getTipo()))
                .count();
    }

    public int getTotalFirmas() {
        return (int) empleados.stream()
                .flatMap(e -> e.getEquipos().stream())
                .filter(eq -> "Firma digital".equalsIgnoreCase(eq.getTipo()))
                .count();
    }
    public int getTotalLectorOptico() {
        return (int) empleados.stream()
                .flatMap(e -> e.getEquipos().stream())
                .filter(eq -> "Lector Optico".equalsIgnoreCase(eq.getTipo()))
                .count();
    }

    public int getTotalImpresoras() {
        return (int) equiposOficina.stream()
                .filter(e -> "IMPRESORA".equalsIgnoreCase(e.getTipo()))
                .count();
    }

    public int getTotalEscaneres() {
        return (int) equiposOficina.stream()
                .filter(e -> "ESCANER".equalsIgnoreCase(e.getTipo()))
                .count();
    }
}
