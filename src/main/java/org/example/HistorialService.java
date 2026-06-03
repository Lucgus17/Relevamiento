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
    private static final int MAX_HISTORIAL = 20; // aumentado a 20 en lugar de 5

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

            // Limpieza segura (asincrónica, sin bloquear)
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

            // PRIMERO: validar que los datos sean coherentes
            if (rel.getNumeroSerialEsperado().isEmpty()) {
                logger.warning("Intento de guardar bienes con lista vacía");
                return;
            }

            // SEGUNDO: crear copias seguras
            List<String> nuevosEsperados = new ArrayList<>(rel.getNumeroSerialEsperado());
            List<String> nuevosEncontrados = new ArrayList<>(rel.getNumeroSerialEncontrado());
            List<String> nuevosSobrantes = new ArrayList<>(rel.getNumeroSerialSobrante());

            // TERCERO: actualizar
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
            // Validación previa
            if (rel == null || rel.getNombre() == null || rel.getNombre().isBlank()) {
                throw new IllegalArgumentException("Nombre de oficina no válido");
            }

            HistorialOficinaEntity e = new HistorialOficinaEntity();
            e.setNombre(rel.getNombre());
            e.setFecha(LocalDateTime.now());

            // Mapear datos
            mapEmpleados(rel, e);
            mapEquiposOficina(rel, e);

            Long id = oficinaRepo.save(e).getId();
            logger.info("Oficina creada con ID: " + id + " nombre: " + rel.getNombre());

            // Limpieza segura (asincrónica)
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

            // SOLUCIÓN: No borrar directamente.
            // En su lugar, mapear nuevos datos a listas nuevas primero
            List<EmpleadoEntity> nuevosEmpleados = new ArrayList<>();
            List<EquipoOficinaEntity> nuevosEquipos = new ArrayList<>();

            // Preparar los nuevos datos en variables temporales
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
                nuevosEmpleados.add(ee);
            }

            for (EquipoOficina eq : rel.getEquiposOficina()) {
                EquipoOficinaEntity eqe = new EquipoOficinaEntity();
                eqe.setTipo(eq.getTipo());
                eqe.setNumeroSerie(eq.getNumeroSerie());
                eqe.setNombre(eq.getNombre());
                nuevosEquipos.add(eqe);
            }

            // AHORA sí, reemplazar de forma segura
            e.setNombre(rel.getNombre());
            e.setFecha(LocalDateTime.now());
            e.getEmpleados().clear();
            e.getEmpleados().addAll(nuevosEmpleados);
            e.getEquiposOficina().clear();
            e.getEquiposOficina().addAll(nuevosEquipos);

            // Un solo save, sin saveAndFlush intermedio
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

    /**
     * Limpieza ASINCRÓNICA - no bloquea la transacción principal
     * Mantiene los últimos 20 registros
     */
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

    /**
     * Limpieza ASINCRÓNICA - no bloquea la transacción principal
     */
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