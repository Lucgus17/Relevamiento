package org.example;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
public class HomeController {

    private final Relevamiento relevamiento = new Relevamiento();

    // ========================================================================
    // INICIO
    // ========================================================================
    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    // ========================================================================
    // INICIAR RELEVAMIENTO
    // ========================================================================
    @PostMapping("/iniciar-relevamiento")
    public String iniciarRelevamiento(
            @RequestParam("nombreRelevamiento") String nombre,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoRelevamiento") String tipo,
            Model model,
            HttpSession session
    ) {
        session.setAttribute("nombreRelevamiento", nombre);

        // ===================== OFICINAS =====================
        if ("OFICINAS".equals(tipo)) {
            // Leer empleados del excel
            List<Empleado> empleados = ExcelService.leerEmpleadosDesdeExcel(archivo);

            // Crear relevamiento oficina
            RelevamientoOficina rel = new RelevamientoOficina(nombre);

            // Iniciar con empleados
            rel.iniciar(nombre, empleados);

            // Guardar en sesión
            session.setAttribute("relevamientoOficina", rel);

            return "redirect:/oficinas/relevamiento";
        }

        // ===================== BIENES =====================
        List<String> seriales = ExcelService.leerSeriales(archivo);
        relevamiento.cargarSeriales(seriales);

        session.setAttribute("relevamientoActivo", true);
        session.setAttribute("ultimoSerial", null);

        return "redirect:/relevamiento";
    }

    // ========================================================================
    // RELEVAMIENTO BIENES - OPERACIONES
    // ========================================================================

    @GetMapping("/relevamiento")
    public String mostrarRelevamiento(Model model, HttpSession session) {
        model.addAttribute("todosLosSeriales", relevamiento.getNumeroSerialEsperado());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante());
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("nombreRelevamiento", session.getAttribute("nombreRelevamiento"));
        model.addAttribute("ultimoSerial", session.getAttribute("ultimoSerial"));

        return "relevamiento";
    }

    @PostMapping("/agregar-serial")
    @ResponseBody
    public Map<String, String> agregarSerial(
            @RequestParam("serial") String serial,
            @RequestParam(value = "accion", required = false) String accion,
            HttpSession session
    ) {
        Map<String, String> response = new HashMap<>();

        if ("encontrado".equals(accion)) {

            relevamiento.marcarComoEncontrado(serial);

            session.setAttribute("ultimoSerial", serial);
            response.put("serialProcesado", serial);
            return response;
        }


        else if ("noInventariado".equals(accion)) {
            // Usuario rechazó la sugerencia, agregar como sobrante
            relevamiento.agregarSobrante(serial);
            response.put("serialProcesado", serial);

        } else {
            // Procesamiento normal con lógica de Levenshtein
            String sugerencia = relevamiento.procesarInputConSugerencia(serial);

            if (sugerencia != null) {
                // Hay sugerencia, devolver al frontend para mostrar modal
                response.put("sugerencia", sugerencia);
                response.put("serialOriginal", serial);
            } else {
                // No hay sugerencia, ya fue procesado
                response.put("serialProcesado", serial);
            }
        }

        session.setAttribute("ultimoSerial", serial);
        return response;
    }

    @GetMapping("/eliminar")
    public String eliminarSerial(@RequestParam String serial) {
        relevamiento.eliminar(serial);
        return "redirect:/relevamiento";
    }

    @PostMapping("/finalizar")
    public String finalizarRelevamiento(HttpSession session, Model model) {
        String nombre = (String) session.getAttribute("nombreRelevamiento");

        model.addAttribute("nombreRelevamiento", nombre);
        model.addAttribute("esperados", relevamiento.getNumeroSerialEsperado().size());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado().size());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante().size());

        session.invalidate();
        return "finalizado";
    }

    // ========================================================================
    // EXPORTACIÓN BIENES - SOLO EXCEL
    // ========================================================================

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcelBienes(
            @RequestParam String nombre,
            HttpSession session
    ) {
        try {
            byte[] excel = ExportService.generarExcel(
                    nombre,
                    relevamiento.getNumeroSerialEsperado(),
                    relevamiento.getNumeroSerialEncontrado(),
                    relevamiento.getNumeroSerialSobrante()
            );

            if (excel == null) {
                return ResponseEntity.status(500).build();
            }

            String filename = ExportService.generarNombreArchivo(nombre, "xlsx");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excel);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================================================
    // EXPORTACIÓN OFICINAS - SOLO EXCEL
    // ========================================================================

    @GetMapping("/oficinas/exportar/excel")
    public ResponseEntity<byte[]> exportarExcelOficinas(HttpSession session) {
        try {
            RelevamientoOficina rel =
                    (RelevamientoOficina) session.getAttribute("relevamientoOficina");

            if (rel == null) {
                return ResponseEntity.status(404).build();
            }

            byte[] excel = ExportService.generarExcelOficinas(rel);

            if (excel == null) {
                return ResponseEntity.status(500).build();
            }

            String filename = ExportService.generarNombreArchivo(rel.getNombre(), "xlsx");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excel);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================================================
    // API JSON - DATOS EN TIEMPO REAL
    // ========================================================================

    @GetMapping("/api/bienes/data")
    @ResponseBody
    public Relevamiento obtenerDatosBienes() {
        return relevamiento;
    }

    @GetMapping("/api/oficinas/data")
    @ResponseBody
    public RelevamientoOficina obtenerDatosOficina(HttpSession session) {
        RelevamientoOficina rel =
                (RelevamientoOficina) session.getAttribute("relevamientoOficina");
        return rel;
    }
}
