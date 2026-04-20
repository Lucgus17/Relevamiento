package org.example;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio(HttpSession session) {
        session.removeAttribute("relevamientoBienes");
        session.removeAttribute("relevamientoOficina");
        session.removeAttribute("nombreRelevamiento");
        session.removeAttribute("ultimoSerial");
        session.removeAttribute("relevamientoActivo");
        return "index";
    }


    @PostMapping("/iniciar-relevamiento")
    public String iniciarRelevamiento(
            @RequestParam("nombreRelevamiento") String nombre,
            @RequestParam("tipoRelevamiento") String tipo,
            @RequestParam(value = "modoOficinas", required = false) String modoOficinas,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo,
            Model model,
            HttpSession session
    ) {
        session.setAttribute("nombreRelevamiento", nombre);


        if ("OFICINAS".equals(tipo)) {
            List<Empleado> empleados = new ArrayList<>();

            if ("CON_EXCEL".equals(modoOficinas)
                    && archivo != null
                    && !archivo.isEmpty()) {
                empleados = ExcelService.leerEmpleadosDesdeExcel(archivo);
            }

            RelevamientoOficina rel = new RelevamientoOficina(nombre);
            rel.iniciar(nombre, empleados);
            session.setAttribute("relevamientoOficina", rel);

            return "redirect:/oficinas/relevamiento";
        }


        if (archivo == null || archivo.isEmpty()) {
            return "redirect:/";
        }

        List<String> seriales = ExcelService.leerSeriales(archivo);
        Relevamiento relevamiento = new Relevamiento();
        relevamiento.cargarSeriales(seriales);

        session.setAttribute("relevamientoBienes", relevamiento);
        session.setAttribute("relevamientoActivo", true);
        session.setAttribute("ultimoSerial", null);

        return "redirect:/relevamiento";
    }


    @GetMapping("/relevamiento")
    public String mostrarRelevamiento(Model model, HttpSession session) {
        Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");
        if (relevamiento == null) return "redirect:/";

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
        } else if ("noInventariado".equals(accion)) {
            relevamiento.agregarSobrante(serial);
            response.put("serialProcesado", serial);
        } else {
            boolean yaEncontrado = relevamiento.getNumeroSerialEncontrado().stream()
                    .anyMatch(s -> s.trim().equalsIgnoreCase(serialNormalizado));
            boolean yaSobrante = relevamiento.getNumeroSerialSobrante().stream()
                    .anyMatch(s -> s.trim().equalsIgnoreCase(serialNormalizado));

            if (yaEncontrado || yaSobrante) {
                response.put("yaExiste", "true");
                response.put("serialProcesado", serial);
                session.setAttribute("ultimoSerial", serial);
                return response;
            }

            String sugerencia = relevamiento.procesarInputConSugerencia(serial);
            if (sugerencia != null) {
                response.put("sugerencia", sugerencia);
                response.put("serialOriginal", serial);
            } else {
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

        if (relevamiento == null) return "redirect:/";

        model.addAttribute("nombreRelevamiento", nombre);
        model.addAttribute("esperados", relevamiento.getNumeroSerialEsperado().size());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado().size());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante().size());

        return "finalizado";
    }


    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcelBienes(HttpSession session) {
        try {
            String nombre = (String) session.getAttribute("nombreRelevamiento");
            Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

            if (relevamiento == null || nombre == null) return ResponseEntity.status(404).build();

            byte[] excel = ExportService.generarExcel(
                    nombre,
                    relevamiento.getNumeroSerialEsperado(),
                    relevamiento.getNumeroSerialEncontrado(),
                    relevamiento.getNumeroSerialSobrante()
            );
            if (excel == null) return ResponseEntity.status(500).build();

            String filename = ExportService.generarNombreArchivo(nombre, "xlsx");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excel);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping("/oficinas/exportar/excel")
    public ResponseEntity<byte[]> exportarExcelOficinas(HttpSession session) {
        try {
            RelevamientoOficina rel =
                    (RelevamientoOficina) session.getAttribute("relevamientoOficina");

            if (rel == null) return ResponseEntity.status(404).build();

            byte[] excel = ExportService.generarExcelOficinas(rel);
            if (excel == null) return ResponseEntity.status(500).build();

            String filename = ExportService.generarNombreArchivo(rel.getNombre(), "xlsx");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excel);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }


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
    public Map<String, Object> obtenerDatosOficina(HttpSession session) {
        RelevamientoOficina rel =
                (RelevamientoOficina) session.getAttribute("relevamientoOficina");

        Map<String, Object> response = new HashMap<>();
        if (rel != null) {
            response.put("empleados", rel.getEmpleados());
            response.put("equiposOficina", rel.getEquiposOficina());
        } else {
            response.put("empleados", new ArrayList<>());
            response.put("equiposOficina", new ArrayList<>());
        }
        return response;
    }

}