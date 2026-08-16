package org.example;

import java.util.ArrayList;
import java.util.List;

public class RelevamientoOficina {

    private String nombre;
    private final List<Oficina> oficinas = new ArrayList<>();

    public RelevamientoOficina(String nombre) {
        this.nombre = nombre;
    }

    public void iniciarConOficinas(String nombre, List<Oficina> oficinasIniciales) {
        this.nombre = nombre;
        oficinas.clear();
        if (oficinasIniciales != null) {
            oficinas.addAll(oficinasIniciales);
        }
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Oficina> getOficinas() { return oficinas; }

    public void agregarOficina(Oficina oficina) { oficinas.add(oficina); }

    public Oficina getOficina(int index) {
        if (index < 0 || index >= oficinas.size()) return null;
        return oficinas.get(index);
    }

    // ── Totales: ahora recorren TODAS las oficinas ──

    public int getTotalCPUs() {
        return contarEquipoEmpleado("CPU");
    }
    public int getTotalMonitores() {
        return contarEquipoEmpleado("Monitor");
    }
    public int getTotalTelefonos() {
        return contarEquipoEmpleado("Teléfono IP");
    }
    public int getTotalCamaras() {
        return contarEquipoEmpleado("Cámara");
    }
    public int getTotalFirmas() {
        return contarEquipoEmpleado("Firma digital");
    }
    public int getTotalLectorOptico() {
        return contarEquipoEmpleado("Lector Optico");
    }

    private int contarEquipoEmpleado(String tipo) {
        return (int) oficinas.stream()
                .flatMap(o -> o.getEmpleados().stream())
                .flatMap(e -> e.getEquipos().stream())
                .filter(eq -> tipo.equalsIgnoreCase(eq.getTipo()))
                .count();
    }

    public int getTotalImpresoras() {
        return contarEquipoOficina("IMPRESORA");
    }
    public int getTotalEscaneres() {
        return contarEquipoOficina("ESCANER");
    }

    private int contarEquipoOficina(String tipo) {
        return (int) oficinas.stream()
                .flatMap(o -> o.getEquiposOficina().stream())
                .filter(eq -> tipo.equalsIgnoreCase(eq.getTipo()))
                .count();
    }
}