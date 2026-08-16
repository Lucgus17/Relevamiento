package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class HistorialService {

    private static final Logger logger = Logger.getLogger(HistorialService.class.getName());
    private static final int MAX_HISTORIAL = 20;

    @Autowired private HistorialBienesRepository bienesRepo;
    @Autowired private HistorialOficinaRepository oficinaRepo;
    @Autowired private TransactionTemplate transactionTemplate;

    // ── BIENES ────────────────────────────────────────────────

    @Transactional
    public Long crearBienes(String nombre, Relevamiento rel) {
        try {
            HistorialBienesEntity e = new HistorialBienesEntity();
            e.setNombre(nombre);
            e.setFecha(LocalDateTime.now());
            e.setEsperados(new ArrayList<>(rel.getNumeroSerialEsperado()));
            e.setEncontrados(new ArrayList<>(rel.getNumeroSerialEncontrado()));
            e.setSobrantes(new ArrayList<>(rel.getNumeroSerialSobrante()));

            Long id = bienesRepo.save(e).getId();
            logger.info("Bienes creado con ID: " + id + " nombre: " + nombre);

            limpiarBienesAsincrono();
            return id;
        } catch (Exception ex) {
            logger.severe("Error al crear bienes: " + ex.getMessage());
            throw new RuntimeException("Fallo al guardar bienes", ex);
        }
    }

    @Transactional
    public void actualizarBienes(Long id, String nombre, Relevamiento rel) {
        try {
            Optional<HistorialBienesEntity> optional = bienesRepo.findById(id);
            if (!optional.isPresent()) {
                logger.warning("Bienes no encontrado con ID: " + id);
                return;
            }

            HistorialBienesEntity e = optional.get();

            if (rel.getNumeroSerialEsperado().isEmpty()) {
                logger.warning("Intento de guardar bienes con lista vacía");
                return;
            }

            List<String> nuevosEsperados = new ArrayList<>(rel.getNumeroSerialEsperado());
            List<String> nuevosEncontrados = new ArrayList<>(rel.getNumeroSerialEncontrado());
            List<String> nuevosSobrantes = new ArrayList<>(rel.getNumeroSerialSobrante());

            e.setNombre(nombre);
            e.setFecha(LocalDateTime.now());
            e.getEsperados().clear();
            e.getEsperados().addAll(nuevosEsperados);
            e.getEncontrados().clear();
            e.getEncontrados().addAll(nuevosEncontrados);
            e.getSobrantes().clear();
            e.getSobrantes().addAll(nuevosSobrantes);

            bienesRepo.save(e);
            logger.info("Bienes actualizado con ID: " + id);

        } catch (Exception ex) {
            logger.severe("Error al actualizar bienes ID " + id + ": " + ex.getMessage());
            throw new RuntimeException("Fallo al actualizar bienes", ex);
        }
    }

    // ── OFICINAS ──────────────────────────────────────────────

    @Transactional
    public Long crearOficina(RelevamientoOficina rel) {
        try {
            if (rel == null || rel.getNombre() == null || rel.getNombre().isBlank()) {
                throw new IllegalArgumentException("Nombre de oficina no válido");
            }

            HistorialOficinaEntity e = new HistorialOficinaEntity();
            e.setNombre(rel.getNombre());
            e.setFecha(LocalDateTime.now());
            e.getOficinas().addAll(mapOficinas(rel));

            Long id = oficinaRepo.save(e).getId();
            logger.info("Oficina creada con ID: " + id + " nombre: " + rel.getNombre());

            limpiarOficinasAsincrono();
            return id;
        } catch (Exception ex) {
            logger.severe("Error al crear oficina: " + ex.getMessage());
            throw new RuntimeException("Fallo al guardar oficina", ex);
        }
    }

    @Transactional
    public void actualizarOficina(Long id, RelevamientoOficina rel) {
        try {
            Optional<HistorialOficinaEntity> optional = oficinaRepo.findById(id);
            if (!optional.isPresent()) {
                logger.warning("Oficina no encontrada con ID: " + id);
                return;
            }

            HistorialOficinaEntity e = optional.get();

            List<OficinaEntity> nuevasOficinas = mapOficinas(rel);

            e.setNombre(rel.getNombre());
            e.setFecha(LocalDateTime.now());
            e.getOficinas().clear();
            e.getOficinas().addAll(nuevasOficinas);

            oficinaRepo.save(e);
            logger.info("Oficina actualizada con ID: " + id);

        } catch (Exception ex) {
            logger.severe("Error al actualizar oficina ID " + id + ": " + ex.getMessage());
            throw new RuntimeException("Fallo al actualizar oficina", ex);
        }
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
        for (String s : e.getSobrantes()) rel.agregarSobrante(s);
        return rel;
    }

    public RelevamientoOficina toRelevamientoOficina(HistorialOficinaEntity e) {
        List<Oficina> oficinas = new ArrayList<>();

        for (OficinaEntity oe : e.getOficinas()) {
            Oficina oficina = new Oficina(oe.getNombre());

            for (EmpleadoEntity ee : oe.getEmpleados()) {
                Empleado emp = new Empleado(ee.getNombre(), ee.getCargo());
                emp.setComentario(ee.getComentario());
                for (EquipoUsuarioEntity eqe : ee.getEquipos()) {
                    emp.agregarEquipo(new EquipoUsuario(eqe.getTipo(), eqe.getNumeroSerie(), eqe.getNombre()));
                }
                oficina.getEmpleados().add(emp);
            }

            for (EquipoOficinaEntity eqe : oe.getEquiposOficina()) {
                oficina.getEquiposOficina().add(
                        new EquipoOficina(eqe.getTipo(), eqe.getNumeroSerie(), eqe.getNombre()));
            }

            oficinas.add(oficina);
        }

        RelevamientoOficina rel = new RelevamientoOficina(e.getNombre());
        rel.iniciarConOficinas(e.getNombre(), oficinas);
        return rel;
    }

    // ── HELPER PRIVADO ────────────────────────────────────────

    private List<OficinaEntity> mapOficinas(RelevamientoOficina rel) {
        List<OficinaEntity> resultado = new ArrayList<>();

        for (Oficina oficina : rel.getOficinas()) {
            OficinaEntity oe = new OficinaEntity(oficina.getNombre());

            for (Empleado emp : oficina.getEmpleados()) {
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
                oe.getEmpleados().add(ee);
            }

            for (EquipoOficina eq : oficina.getEquiposOficina()) {
                EquipoOficinaEntity eqe = new EquipoOficinaEntity();
                eqe.setTipo(eq.getTipo());
                eqe.setNumeroSerie(eq.getNumeroSerie());
                eqe.setNombre(eq.getNombre());
                oe.getEquiposOficina().add(eqe);
            }

            resultado.add(oe);
        }

        return resultado;
    }

    // ── LIMPIEZA ASINCRÓNICA ──────────────────────────────────

    private void limpiarBienesAsincrono() {
        new Thread(() -> {
            try {
                List<HistorialBienesEntity> todos = bienesRepo.findAllByOrderByFechaDesc();
                if (todos.size() > MAX_HISTORIAL) {
                    List<HistorialBienesEntity> aEliminar = todos.subList(MAX_HISTORIAL, todos.size());
                    logger.info("Limpiando " + aEliminar.size() + " registros de bienes antiguos");
                    bienesRepo.deleteAll(aEliminar);
                }
            } catch (Exception ex) {
                logger.severe("Error en limpieza asincrónica de bienes: " + ex.getMessage());
            }
        }).start();
    }

    private void limpiarOficinasAsincrono() {
        new Thread(() -> {
            try {
                List<HistorialOficinaEntity> todos = oficinaRepo.findAllByOrderByFechaDesc();
                if (todos.size() > MAX_HISTORIAL) {
                    List<HistorialOficinaEntity> aEliminar = todos.subList(MAX_HISTORIAL, todos.size());
                    logger.info("Limpiando " + aEliminar.size() + " registros de oficinas antiguos");
                    oficinaRepo.deleteAll(aEliminar);
                }
            } catch (Exception ex) {
                logger.severe("Error en limpieza asincrónica de oficinas: " + ex.getMessage());
            }
        }).start();
    }
}