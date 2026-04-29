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

@Controller
@RequestMapping("/oficinas")
public class OficinaController {

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
        Long id = (Long) session.getAttribute("historialOficinaId");
        if (id == null) return;
        RelevamientoOficina rel = (RelevamientoOficina) session.getAttribute("relevamientoOficina");
        if (rel != null) historialService.actualizarOficina(id, rel);
    }

    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> obtenerDatos(HttpSession session) {
        RelevamientoOficina rel = obtenerRelevamiento(session);
        Map<String, Object> response = new HashMap<>();
        response.put("empleados", rel.getEmpleados());
        response.put("equiposOficina", rel.getEquiposOficina());
        return response;
    }

    @GetMapping("/relevamiento")
    public String mostrarRelevamiento(HttpSession session, Model model) {
        RelevamientoOficina rel = obtenerRelevamiento(session);
        model.addAttribute("nombreRelevamiento", rel.getNombre());
        model.addAttribute("empleados", rel.getEmpleados());
        model.addAttribute("equiposOficina", rel.getEquiposOficina());
        return "relevamiento-oficinas";
    }

    @PostMapping("/empleado")
    public String agregarEmpleado(@RequestParam String nombre, HttpSession session) {
        if (nombre == null || nombre.isBlank()) return "redirect:/oficinas/relevamiento";
        RelevamientoOficina rel = obtenerRelevamiento(session);
        rel.getEmpleados().add(new Empleado(nombre.trim()));
        autoguardarOficina(session);
        return "redirect:/oficinas/relevamiento";
    }

    @PostMapping("/eliminar-empleado")
    @ResponseBody
    public Map<String, String> eliminarEmpleado(@RequestParam int indexEmpleado, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        RelevamientoOficina rel = obtenerRelevamiento(session);
        if (indexEmpleado < 0 || indexEmpleado >= rel.getEmpleados().size()) {
            response.put("error", "Índice inválido");
            return response;
        }
        rel.getEmpleados().remove(indexEmpleado);
        autoguardarOficina(session);
        response.put("success", "true");
        return response;
    }

    @PostMapping("/comentario-empleado")
    @ResponseBody
    public Map<String, Object> guardarComentario(
            @RequestParam int indexEmpleado,
            @RequestParam(defaultValue = "") String comentario,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        RelevamientoOficina rel = obtenerRelevamiento(session);
        if (indexEmpleado < 0 || indexEmpleado >= rel.getEmpleados().size()) {
            response.put("error", "Índice inválido");
            return response;
        }
        rel.getEmpleados().get(indexEmpleado).setComentario(comentario.trim());
        autoguardarOficina(session);
        response.put("ok", true);
        return response;
    }

    @PostMapping("/equipo-usuario")
    public String agregarEquipoUsuario(
            @RequestParam int indexEmpleado,
            @RequestParam String tipo,
            @RequestParam String numeroSerie,
            @RequestParam(required = false) String nombre,
            HttpSession session
    ) {
        RelevamientoOficina rel = obtenerRelevamiento(session);
        if (indexEmpleado < 0 || indexEmpleado >= rel.getEmpleados().size()
                || numeroSerie == null || numeroSerie.isBlank()) {
            return "redirect:/oficinas/relevamiento";
        }
        rel.getEmpleados().get(indexEmpleado).agregarEquipo(new EquipoUsuario(tipo, numeroSerie.trim(), nombre));
        autoguardarOficina(session);
        return "redirect:/oficinas/relevamiento";
    }

    @PostMapping("/equipo-oficina")
    public String agregarEquipoOficina(
            @RequestParam String tipo,
            @RequestParam String numeroSerie,
            @RequestParam(required = false) String nombre,
            HttpSession session
    ) {
        if (numeroSerie == null || numeroSerie.isBlank()) return "redirect:/oficinas/relevamiento";
        RelevamientoOficina rel = obtenerRelevamiento(session);
        rel.getEquiposOficina().add(new EquipoOficina(tipo, numeroSerie.trim(), nombre));
        autoguardarOficina(session);
        return "redirect:/oficinas/relevamiento";
    }

    @PostMapping("/eliminar-equipo-usuario")
    public String eliminarEquipoUsuario(
            @RequestParam int indexEmpleado,
            @RequestParam int indexEquipo,
            HttpSession session
    ) {
        RelevamientoOficina rel = obtenerRelevamiento(session);
        if (indexEmpleado < 0 || indexEmpleado >= rel.getEmpleados().size()) return "redirect:/oficinas/relevamiento";
        List<EquipoUsuario> equipos = rel.getEmpleados().get(indexEmpleado).getEquipos();
        if (indexEquipo < 0 || indexEquipo >= equipos.size()) return "redirect:/oficinas/relevamiento";
        equipos.remove(indexEquipo);
        autoguardarOficina(session);
        return "redirect:/oficinas/relevamiento";
    }

    @PostMapping("/eliminar-equipo-oficina")
    public String eliminarEquipoOficina(@RequestParam int indexEquipo, HttpSession session) {
        RelevamientoOficina rel = obtenerRelevamiento(session);
        if (indexEquipo < 0 || indexEquipo >= rel.getEquiposOficina().size()) return "redirect:/oficinas/relevamiento";
        rel.getEquiposOficina().remove(indexEquipo);
        autoguardarOficina(session);
        return "redirect:/oficinas/relevamiento";
    }

    @GetMapping("/finalizado")
    public String finalizado(HttpSession session, Model model) {
        RelevamientoOficina rel = (RelevamientoOficina) session.getAttribute("relevamientoOficina");
        if (rel == null) return "redirect:/";
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
        return "finalizado-oficinas";
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
        } catch (Exception e) {
            Throwable causa = e;
            while (causa != null) {
                if (causa instanceof java.net.UnknownHostException
                        || causa instanceof java.net.ConnectException
                        || causa.getClass().getName().contains("MailConnectException")) {
                    res.put("error", "SIN_INTERNET");
                    return res;
                }
                causa = causa.getCause();
            }
            res.put("error", "No se pudo enviar: " + e.getMessage());
        }
        return res;
    }
}