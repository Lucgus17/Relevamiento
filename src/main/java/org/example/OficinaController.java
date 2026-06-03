package org.example;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/oficinas")
public class OficinaController {

    private static final Logger logger = Logger.getLogger(OficinaController.class.getName());

    @Autowired private HistorialService historialService;
    @Autowired private MailService mailService;

    private RelevamientoOficina obtenerRelevamiento(HttpSession session) {
        RelevamientoOficina rel = (RelevamientoOficina) session.getAttribute("relevamientoOficina");
        if (rel == null) {
            String nombre = (String) session.getAttribute("nombreRelevamiento");
            if (nombre == null || nombre.isBlank()) nombre = "Relevamiento sin nombre";
            rel = new RelevamientoOficina(nombre);
            session.setAttribute("relevamientoOficina", rel);
        }
        return rel;
    }

    private void autoguardarOficina(HttpSession session) {
        try {
            Long id = (Long) session.getAttribute("historialOficinaId");
            if (id == null) {
                logger.warning("No hay ID de historial para guardar oficina");
                return;
            }

            RelevamientoOficina rel = (RelevamientoOficina) session.getAttribute("relevamientoOficina");
            if (rel != null) {
                historialService.actualizarOficina(id, rel);
                logger.fine("Autoguardado de oficina exitoso - ID: " + id);
            }
        } catch (Exception ex) {
            logger.severe("Error en autoguardado de oficina: " + ex.getMessage());
            // No lanzar excepción, permitir continuar
        }
    }

    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> obtenerDatos(HttpSession session) {
        try {
            RelevamientoOficina rel = obtenerRelevamiento(session);
            Map<String, Object> response = new HashMap<>();
            response.put("empleados", rel.getEmpleados());
            response.put("equiposOficina", rel.getEquiposOficina());
            return response;
        } catch (Exception ex) {
            logger.severe("Error en obtenerDatos: " + ex.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error al obtener datos");
            return response;
        }
    }

    @GetMapping("/relevamiento")
    public String mostrarRelevamiento(HttpSession session, Model model) {
        try {
            RelevamientoOficina rel = obtenerRelevamiento(session);
            model.addAttribute("nombreRelevamiento", rel.getNombre());
            model.addAttribute("empleados", rel.getEmpleados());
            model.addAttribute("equiposOficina", rel.getEquiposOficina());
            return "relevamiento-oficinas";
        } catch (Exception ex) {
            logger.severe("Error en mostrarRelevamiento: " + ex.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/empleado")
    public String agregarEmpleado(@RequestParam String nombre, HttpSession session) {
        try {
            if (nombre == null || nombre.isBlank()) {
                logger.warning("Intento de agregar empleado con nombre vacío");
                return "redirect:/oficinas/relevamiento";
            }

            RelevamientoOficina rel = obtenerRelevamiento(session);
            Empleado nuevoEmpleado = new Empleado(nombre.trim());
            rel.getEmpleados().add(nuevoEmpleado);
            autoguardarOficina(session);

            logger.info("Empleado agregado: " + nombre);
            return "redirect:/oficinas/relevamiento";

        } catch (Exception ex) {
            logger.severe("Error al agregar empleado: " + ex.getMessage());
            return "redirect:/oficinas/relevamiento";
        }
    }

    @PostMapping("/eliminar-empleado")
    @ResponseBody
    public Map<String, String> eliminarEmpleado(@RequestParam int indexEmpleado, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        try {
            RelevamientoOficina rel = obtenerRelevamiento(session);

            if (indexEmpleado < 0 || indexEmpleado >= rel.getEmpleados().size()) {
                response.put("error", "Índice inválido");
                return response;
            }

            Empleado eliminado = rel.getEmpleados().remove(indexEmpleado);
            autoguardarOficina(session);

            response.put("success", "true");
            logger.info("Empleado eliminado: " + (eliminado != null ? eliminado.getNombre() : "desconocido"));
            return response;

        } catch (Exception ex) {
            logger.severe("Error al eliminar empleado: " + ex.getMessage());
            response.put("error", "Error al eliminar");
            return response;
        }
    }

    @PostMapping("/comentario-empleado")
    @ResponseBody
    public Map<String, Object> guardarComentario(
            @RequestParam int indexEmpleado,
            @RequestParam(defaultValue = "") String comentario,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            RelevamientoOficina rel = obtenerRelevamiento(session);

            if (indexEmpleado < 0 || indexEmpleado >= rel.getEmpleados().size()) {
                response.put("error", "Índice inválido");
                return response;
            }

            rel.getEmpleados().get(indexEmpleado).setComentario(comentario.trim());
            autoguardarOficina(session);

            response.put("ok", true);
            logger.fine("Comentario guardado para empleado índice: " + indexEmpleado);
            return response;

        } catch (Exception ex) {
            logger.severe("Error al guardar comentario: " + ex.getMessage());
            response.put("error", "Error al guardar");
            return response;
        }
    }

    @PostMapping("/equipo-usuario")
    public String agregarEquipoUsuario(
            @RequestParam int indexEmpleado,
            @RequestParam String tipo,
            @RequestParam String numeroSerie,
            @RequestParam(required = false) String nombre,
            HttpSession session
    ) {
        try {
            RelevamientoOficina rel = obtenerRelevamiento(session);

            // Validaciones
            if (indexEmpleado < 0 || indexEmpleado >= rel.getEmpleados().size()) {
                logger.warning("Índice de empleado inválido: " + indexEmpleado);
                return "redirect:/oficinas/relevamiento";
            }

            if (numeroSerie == null || numeroSerie.isBlank()) {
                logger.warning("Intento de agregar equipo sin número de serie");
                return "redirect:/oficinas/relevamiento";
            }

            if (tipo == null || tipo.isBlank()) {
                logger.warning("Intento de agregar equipo sin tipo");
                return "redirect:/oficinas/relevamiento";
            }

            EquipoUsuario equipo = new EquipoUsuario(tipo.trim(), numeroSerie.trim(), nombre);
            rel.getEmpleados().get(indexEmpleado).agregarEquipo(equipo);
            autoguardarOficina(session);

            logger.info("Equipo agregado al empleado " + indexEmpleado + ": " + tipo);
            return "redirect:/oficinas/relevamiento";

        } catch (Exception ex) {
            logger.severe("Error al agregar equipo de usuario: " + ex.getMessage());
            return "redirect:/oficinas/relevamiento";
        }
    }

    @PostMapping("/equipo-oficina")
    public String agregarEquipoOficina(
            @RequestParam String tipo,
            @RequestParam String numeroSerie,
            @RequestParam(required = false) String nombre,
            HttpSession session
    ) {
        try {
            // Validaciones
            if (numeroSerie == null || numeroSerie.isBlank()) {
                logger.warning("Intento de agregar equipo de oficina sin número de serie");
                return "redirect:/oficinas/relevamiento";
            }

            if (tipo == null || tipo.isBlank()) {
                logger.warning("Intento de agregar equipo de oficina sin tipo");
                return "redirect:/oficinas/relevamiento";
            }

            RelevamientoOficina rel = obtenerRelevamiento(session);
            EquipoOficina equipo = new EquipoOficina(tipo.trim(), numeroSerie.trim(), nombre);
            rel.getEquiposOficina().add(equipo);
            autoguardarOficina(session);

            logger.info("Equipo de oficina agregado: " + tipo);
            return "redirect:/oficinas/relevamiento";

        } catch (Exception ex) {
            logger.severe("Error al agregar equipo de oficina: " + ex.getMessage());
            return "redirect:/oficinas/relevamiento";
        }
    }

    @PostMapping("/eliminar-equipo-usuario")
    public String eliminarEquipoUsuario(
            @RequestParam int indexEmpleado,
            @RequestParam int indexEquipo,
            HttpSession session
    ) {
        try {
            RelevamientoOficina rel = obtenerRelevamiento(session);

            if (indexEmpleado < 0 || indexEmpleado >= rel.getEmpleados().size()) {
                logger.warning("Índice de empleado inválido: " + indexEmpleado);
                return "redirect:/oficinas/relevamiento";
            }

            List<EquipoUsuario> equipos = rel.getEmpleados().get(indexEmpleado).getEquipos();

            if (indexEquipo < 0 || indexEquipo >= equipos.size()) {
                logger.warning("Índice de equipo inválido: " + indexEquipo);
                return "redirect:/oficinas/relevamiento";
            }

            EquipoUsuario eliminado = equipos.remove(indexEquipo);
            autoguardarOficina(session);

            logger.info("Equipo de usuario eliminado: " + (eliminado != null ? eliminado.getTipo() : "desconocido"));
            return "redirect:/oficinas/relevamiento";

        } catch (Exception ex) {
            logger.severe("Error al eliminar equipo de usuario: " + ex.getMessage());
            return "redirect:/oficinas/relevamiento";
        }
    }

    @PostMapping("/eliminar-equipo-oficina")
    public String eliminarEquipoOficina(@RequestParam int indexEquipo, HttpSession session) {
        try {
            RelevamientoOficina rel = obtenerRelevamiento(session);

            if (indexEquipo < 0 || indexEquipo >= rel.getEquiposOficina().size()) {
                logger.warning("Índice de equipo de oficina inválido: " + indexEquipo);
                return "redirect:/oficinas/relevamiento";
            }

            EquipoOficina eliminado = rel.getEquiposOficina().remove(indexEquipo);
            autoguardarOficina(session);

            logger.info("Equipo de oficina eliminado: " + (eliminado != null ? eliminado.getTipo() : "desconocido"));
            return "redirect:/oficinas/relevamiento";

        } catch (Exception ex) {
            logger.severe("Error al eliminar equipo de oficina: " + ex.getMessage());
            return "redirect:/oficinas/relevamiento";
        }
    }

    @GetMapping("/finalizado")
    public String finalizado(HttpSession session, Model model) {
        try {
            RelevamientoOficina rel = (RelevamientoOficina) session.getAttribute("relevamientoOficina");
            if (rel == null) {
                logger.warning("Intento de finalizar sin relevamiento");
                return "redirect:/";
            }

            // Guardar antes de finalizar
            autoguardarOficina(session);

            model.addAttribute("nombreRelevamiento", rel.getNombre());
            model.addAttribute("totalEmpleados", rel.getEmpleados().size());
            model.addAttribute("totalCPUs", rel.getTotalCPUs());
            model.addAttribute("totalMonitores", rel.getTotalMonitores());
            model.addAttribute("totalTelefonos", rel.getTotalTelefonos());
            model.addAttribute("totalCamaras", rel.getTotalCamaras());
            model.addAttribute("totalFirmas", rel.getTotalFirmas());
            model.addAttribute("totalLectorOptico", rel.getTotalLectorOptico());
            model.addAttribute("totalImpresoras", rel.getTotalImpresoras());
            model.addAttribute("totalEscaneres", rel.getTotalEscaneres());

            session.setAttribute("yaGuardado", true);
            logger.info("Relevamiento de oficina finalizado: " + rel.getNombre());

            return "finalizado-oficinas";

        } catch (Exception ex) {
            logger.severe("Error finalizando oficinas: " + ex.getMessage());
            model.addAttribute("error", "Error al finalizar");
            return "finalizado-oficinas";
        }
    }

    @PostMapping("/enviar-excel")
    @ResponseBody
    public Map<String, String> enviarExcelOficinas(
            @RequestParam String prefijo,
            HttpSession session
    ) {
        Map<String, String> res = new HashMap<>();
        try {
            RelevamientoOficina rel = (RelevamientoOficina) session.getAttribute("relevamientoOficina");
            if (rel == null) {
                res.put("error", "No hay relevamiento activo");
                return res;
            }

            // Guardar antes de enviar
            autoguardarOficina(session);

            byte[] excel = ExportService.generarExcelOficinas(rel);
            if (excel == null) {
                res.put("error", "Error al generar el Excel");
                return res;
            }

            String destinatario = prefijo.trim() + "@justiciacordoba.gob.ar";
            String nombreArchivo = ExportService.generarNombreArchivo(rel.getNombre(), "xlsx");
            mailService.enviarExcel(destinatario, rel.getNombre(), excel, nombreArchivo);

            res.put("ok", "true");
            res.put("destinatario", destinatario);
            logger.info("Excel de oficinas enviado a: " + destinatario);

        } catch (Exception e) {
            Throwable causa = e;
            while (causa != null) {
                if (causa instanceof java.net.UnknownHostException
                        || causa instanceof java.net.ConnectException
                        || causa.getClass().getName().contains("MailConnectException")) {
                    res.put("error", "SIN_INTERNET");
                    logger.warning("Error de conectividad al enviar Excel");
                    return res;
                }
                causa = causa.getCause();
            }
            logger.severe("Error enviando Excel de oficinas: " + e.getMessage());
            res.put("error", "No se pudo enviar: " + e.getMessage());
        }
        return res;
    }
}