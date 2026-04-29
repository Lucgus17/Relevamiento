package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HistorialService {

    @Autowired private HistorialBienesRepository bienesRepo;
    @Autowired private HistorialOficinaRepository oficinaRepo;

    // ── BIENES ────────────────────────────────────────────────

    @Transactional
    public Long crearBienes(String nombre, Relevamiento rel) {
        HistorialBienesEntity e = new HistorialBienesEntity();
        e.setNombre(nombre);
        e.setFecha(LocalDateTime.now());
        e.setEsperados(new ArrayList<>(rel.getNumeroSerialEsperado()));
        e.setEncontrados(new ArrayList<>(rel.getNumeroSerialEncontrado()));
        e.setSobrantes(new ArrayList<>(rel.getNumeroSerialSobrante()));
        Long id = bienesRepo.save(e).getId();
        limpiarBienes();
        return id;
    }

    @Transactional
    public void actualizarBienes(Long id, String nombre, Relevamiento rel) {
        bienesRepo.findById(id).ifPresent(e -> {
            e.setNombre(nombre);
            e.setFecha(LocalDateTime.now());
            e.getEsperados().clear();
            e.getEsperados().addAll(rel.getNumeroSerialEsperado());
            e.getEncontrados().clear();
            e.getEncontrados().addAll(rel.getNumeroSerialEncontrado());
            e.getSobrantes().clear();
            e.getSobrantes().addAll(rel.getNumeroSerialSobrante());
            bienesRepo.save(e);
        });
    }

    // ── OFICINAS ──────────────────────────────────────────────

    @Transactional
    public Long crearOficina(RelevamientoOficina rel) {
        HistorialOficinaEntity e = new HistorialOficinaEntity();
        e.setNombre(rel.getNombre());
        e.setFecha(LocalDateTime.now());
        mapEmpleados(rel, e);
        mapEquiposOficina(rel, e);
        Long id = oficinaRepo.save(e).getId();
        limpiarOficinas();
        return id;
    }

    @Transactional
    public void actualizarOficina(Long id, RelevamientoOficina rel) {
        oficinaRepo.findById(id).ifPresent(e -> {
            e.setNombre(rel.getNombre());
            e.setFecha(LocalDateTime.now());
            e.getEmpleados().clear();
            e.getEquiposOficina().clear();
            // flush para que JPA elimine los huérfanos antes de insertar los nuevos
            oficinaRepo.saveAndFlush(e);
            mapEmpleados(rel, e);
            mapEquiposOficina(rel, e);
            oficinaRepo.save(e);
        });
    }

    // ── CONSULTAS ─────────────────────────────────────────────

    public List<HistorialBienesEntity> listarBienes() {
        return bienesRepo.findAllByOrderByFechaDesc();
    }

    public List<HistorialOficinaEntity> listarOficinas() {
        return oficinaRepo.findAllByOrderByFechaDesc();
    }

    public Optional<HistorialBienesEntity> findBienes(Long id) {
        return bienesRepo.findById(id);
    }

    public Optional<HistorialOficinaEntity> findOficina(Long id) {
        return oficinaRepo.findById(id);
    }

    // ── CONVERSIÓN ────────────────────────────────────────────

    public Relevamiento toBienes(HistorialBienesEntity e) {
        Relevamiento rel = new Relevamiento();
        rel.cargarSeriales(new ArrayList<>(e.getEsperados()));
        for (String s : e.getEncontrados()) rel.marcarComoEncontrado(s);
        for (String s : e.getSobrantes())   rel.agregarSobrante(s);
        return rel;
    }

    public RelevamientoOficina toRelevamientoOficina(HistorialOficinaEntity e) {
        List<Empleado> empleados = new ArrayList<>();
        for (EmpleadoEntity ee : e.getEmpleados()) {
            Empleado emp = new Empleado(ee.getNombre(), ee.getCargo());
            emp.setComentario(ee.getComentario());
            for (EquipoUsuarioEntity eqe : ee.getEquipos()) {
                emp.agregarEquipo(new EquipoUsuario(eqe.getTipo(), eqe.getNumeroSerie(), eqe.getNombre()));
            }
            empleados.add(emp);
        }
        RelevamientoOficina rel = new RelevamientoOficina(e.getNombre());
        rel.iniciar(e.getNombre(), empleados);
        for (EquipoOficinaEntity eqe : e.getEquiposOficina()) {
            rel.getEquiposOficina().add(new EquipoOficina(eqe.getTipo(), eqe.getNumeroSerie(), eqe.getNombre()));
        }
        return rel;
    }

    // ── HELPERS PRIVADOS ──────────────────────────────────────

    private void mapEmpleados(RelevamientoOficina rel, HistorialOficinaEntity e) {
        for (Empleado emp : rel.getEmpleados()) {
            EmpleadoEntity ee = new EmpleadoEntity();
            ee.setNombre(emp.getNombre());
            ee.setCargo(emp.getCargo());
            ee.setComentario(emp.getComentario());
            for (EquipoUsuario eq : emp.getEquipos()) {
                EquipoUsuarioEntity eqe = new EquipoUsuarioEntity();
                eqe.setTipo(eq.getTipo());
                eqe.setNumeroSerie(eq.getNumeroSerie());
                eqe.setNombre(eq.getNombre());
                ee.getEquipos().add(eqe);
            }
            e.getEmpleados().add(ee);
        }
    }

    private void mapEquiposOficina(RelevamientoOficina rel, HistorialOficinaEntity e) {
        for (EquipoOficina eq : rel.getEquiposOficina()) {
            EquipoOficinaEntity eqe = new EquipoOficinaEntity();
            eqe.setTipo(eq.getTipo());
            eqe.setNumeroSerie(eq.getNumeroSerie());
            eqe.setNombre(eq.getNombre());
            e.getEquiposOficina().add(eqe);
        }
    }

    private void limpiarBienes() {
        List<HistorialBienesEntity> todos = bienesRepo.findAllByOrderByFechaDesc();
        if (todos.size() > 5) {
            bienesRepo.deleteAll(todos.subList(5, todos.size()));
        }
    }

    private void limpiarOficinas() {
        List<HistorialOficinaEntity> todos = oficinaRepo.findAllByOrderByFechaDesc();
        if (todos.size() > 5) {
            oficinaRepo.deleteAll(todos.subList(5, todos.size()));
        }
    }
}