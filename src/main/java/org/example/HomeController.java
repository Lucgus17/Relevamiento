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

    // ========================================================================
    // INICIO
    // ========================================================================
    @GetMapping("/")
    public String inicio(HttpSession session) {
        // Limpiar relevamientos anteriores cuando se vuelve a inicio
        session.removeAttribute("relevamientoBienes");
        session.removeAttribute("relevamientoOficina");
        session.removeAttribute("nombreRelevamiento");
        session.removeAttribute("ultimoSerial");
        session.removeAttribute("relevamientoActivo");

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

        // ⚠️ CREAR NUEVA INSTANCIA EN LUGAR DE USAR SINGLETON
        Relevamiento relevamiento = new Relevamiento();
        relevamiento.cargarSeriales(seriales);

        // Guardar en sesión
        session.setAttribute("relevamientoBienes", relevamiento);
        session.setAttribute("relevamientoActivo", true);
        session.setAttribute("ultimoSerial", null);

        return "redirect:/relevamiento";
    }

    // ========================================================================
    // RELEVAMIENTO BIENES - OPERACIONES
    // ========================================================================

    @GetMapping("/relevamiento")
    public String mostrarRelevamiento(Model model, HttpSession session) {
        Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

        if (relevamiento == null) {
            return "redirect:/";
        }

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

        Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

        if (relevamiento == null) {
            response.put("error", "No hay relevamiento activo");
            return response;
        }

        String serialNormalizado = serial.trim().toUpperCase();

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
            // VERIFICAR SI YA EXISTE EN ENCONTRADOS O SOBRANTES
            boolean yaEncontrado = relevamiento.getNumeroSerialEncontrado().stream()
                    .anyMatch(s -> s.trim().equalsIgnoreCase(serialNormalizado));

            boolean yaSobrante = relevamiento.getNumeroSerialSobrante().stream()
                    .anyMatch(s -> s.trim().equalsIgnoreCase(serialNormalizado));

            if (yaEncontrado || yaSobrante) {
                // Ya existe, solo resaltarlo
                response.put("yaExiste", "true");
                response.put("serialProcesado", serial);
                session.setAttribute("ultimoSerial", serial);
                return response;
            }

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

    @PostMapping("/eliminar-serial")
    @ResponseBody
    public Map<String, String> eliminarSerial(
            @RequestParam String serial,
            HttpSession session
    ) {
        Map<String, String> response = new HashMap<>();

        Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

        if (relevamiento == null) {
            response.put("error", "No hay relevamiento activo");
            return response;
        }

        relevamiento.eliminar(serial);
        response.put("success", "true");
        return response;
    }

    @PostMapping("/finalizar")
    public String finalizarRelevamiento(HttpSession session, Model model) {
        String nombre = (String) session.getAttribute("nombreRelevamiento");
        Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

        if (relevamiento == null) {
            return "redirect:/";
        }

        model.addAttribute("nombreRelevamiento", nombre);
        model.addAttribute("esperados", relevamiento.getNumeroSerialEsperado().size());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado().size());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante().size());

        // ⭐ Mantener el relevamiento en sesión para que persista al volver con flecha
        return "finalizado";
    }

    // ========================================================================
    // EXPORTACIÓN BIENES - SOLO EXCEL
    // ========================================================================

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcelBienes(HttpSession session) {
        try {
            String nombre = (String) session.getAttribute("nombreRelevamiento");
            Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

            if (relevamiento == null || nombre == null) {
                return ResponseEntity.status(404).build();
            }

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
    public Map<String, Object> obtenerDatosBienes(HttpSession session) {
        Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");
        String nombre = (String) session.getAttribute("nombreRelevamiento");

        Map<String, Object> response = new HashMap<>();

        if (relevamiento != null) {
            response.put("esperados", relevamiento.getNumeroSerialEsperado());
            response.put("encontrados", relevamiento.getNumeroSerialEncontrado());
            response.put("sobrantes", relevamiento.getNumeroSerialSobrante());
            response.put("conteos", relevamiento.contarNumerosSeriales());
            response.put("nombreRelevamiento", nombre);
        }

        return response;
    }

    @GetMapping("/api/oficinas/data")
    @ResponseBody
    public RelevamientoOficina obtenerDatosOficina(HttpSession session) {
        RelevamientoOficina rel =
                (RelevamientoOficina) session.getAttribute("relevamientoOficina");
        return rel;
    }
}