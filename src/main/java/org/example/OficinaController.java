package org.example;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/oficinas")
public class OficinaController {

    // ================= UTIL =================
    private RelevamientoOficina obtenerRelevamiento(HttpSession session) {

        RelevamientoOficina rel =
                (RelevamientoOficina) session.getAttribute("relevamientoOficina");

        if (rel == null) {

            String nombre =
                    (String) session.getAttribute("nombreRelevamiento");

            if (nombre == null || nombre.isBlank()) {
                nombre = "Relevamiento sin nombre";
            }

            rel = new RelevamientoOficina(nombre);
            session.setAttribute("relevamientoOficina", rel);
        }

        return rel;
    }

    // ================= API ENDPOINT PARA CARGAR DATOS =================
    @GetMapping("/api/oficinas/data")
    @ResponseBody
    public Map<String, Object> obtenerDatos(HttpSession session) {
        RelevamientoOficina rel = obtenerRelevamiento(session);

        Map<String, Object> response = new HashMap<>();
        response.put("empleados", rel.getEmpleados());
        response.put("equiposOficina", rel.getEquiposOficina());

        return response;
    }

    // ================= MOSTRAR =================
    @GetMapping("/relevamiento")
    public String mostrarRelevamiento(HttpSession session, Model model) {

        RelevamientoOficina rel = obtenerRelevamiento(session);

        model.addAttribute("nombreRelevamiento", rel.getNombre());
        model.addAttribute("empleados", rel.getEmpleados());
        model.addAttribute("equiposOficina", rel.getEquiposOficina());

        return "relevamiento-oficinas";
    }

    // ================= CARGAR EXCEL =================
    @PostMapping("/cargar-excel")
    public String cargarExcel(
            @RequestParam MultipartFile archivo,
            @RequestParam String nombreRelevamiento,
            HttpSession session
    ) {

        List<Empleado> empleados =
                ExcelService.leerEmpleadosDesdeExcel(archivo);

        RelevamientoOficina rel = obtenerRelevamiento(session);
        rel.iniciar(nombreRelevamiento, empleados);

        return "redirect:/oficinas/relevamiento";
    }

    // ================= AGREGAR EMPLEADO =================
    @PostMapping("/empleado")
    public String agregarEmpleado(
            @RequestParam String nombre,
            HttpSession session
    ) {

        if (nombre == null || nombre.isBlank()) {
            return "redirect:/oficinas/relevamiento";
        }

        RelevamientoOficina rel = obtenerRelevamiento(session);
        rel.getEmpleados().add(new Empleado(nombre.trim()));

        return "redirect:/oficinas/relevamiento";
    }

    // ================= EQUIPO USUARIO =================
    @PostMapping("/equipo-usuario")
    public String agregarEquipoUsuario(
            @RequestParam int indexEmpleado,
            @RequestParam String tipo,
            @RequestParam String numeroSerie,
            @RequestParam(required = false) String nombre,
            HttpSession session
    ) {

        RelevamientoOficina rel = obtenerRelevamiento(session);

        if (indexEmpleado < 0 || indexEmpleado >= rel.getEmpleados().size()) {
            return "redirect:/oficinas/relevamiento";
        }

        if (numeroSerie == null || numeroSerie.isBlank()) {
            return "redirect:/oficinas/relevamiento";
        }

        rel.getEmpleados().get(indexEmpleado)
                .agregarEquipo(new EquipoUsuario(
                        tipo,
                        numeroSerie.trim(),
                        nombre
                ));

        return "redirect:/oficinas/relevamiento";
    }

    // ================= EQUIPO OFICINA =================
    @PostMapping("/equipo-oficina")
    public String agregarEquipoOficina(
            @RequestParam String tipo,
            @RequestParam String numeroSerie,
            @RequestParam(required = false) String nombre,
            HttpSession session
    ) {

        if (numeroSerie == null || numeroSerie.isBlank()) {
            return "redirect:/oficinas/relevamiento";
        }

        RelevamientoOficina rel = obtenerRelevamiento(session);

        rel.getEquiposOficina().add(
                new EquipoOficina(tipo, numeroSerie.trim(), nombre)
        );

        return "redirect:/oficinas/relevamiento";
    }

    // ================= FINALIZADO =================
    @GetMapping("/finalizado")
    public String finalizado(HttpSession session, Model model) {

        RelevamientoOficina rel =
                (RelevamientoOficina) session.getAttribute("relevamientoOficina");

        if (rel == null) return "redirect:/";

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
}