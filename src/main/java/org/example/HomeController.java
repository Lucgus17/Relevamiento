package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class HomeController {

    private static final Logger logger = Logger.getLogger(HomeController.class.getName());

    @Autowired private HistorialService historialService;
    @Autowired private MailService mailService;

    @GetMapping("/")
    public String inicio(HttpSession session) {
        limpiarSesion(session);
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
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                model.addAttribute("error", "El nombre del relevamiento es obligatorio");
                return "index";
            }

            nombre = nombre.trim();
            session.setAttribute("nombreRelevamiento", nombre);

            if ("OFICINAS".equals(tipo)) {
                List<Oficina> oficinas = new ArrayList<>();
                if ("CON_EXCEL".equals(modoOficinas) && archivo != null && !archivo.isEmpty()) {
                    oficinas = ExcelService.leerOficinasDesdeExcel(archivo);
                }

                RelevamientoOficina rel = new RelevamientoOficina(nombre);
                rel.iniciarConOficinas(nombre, oficinas);
                session.setAttribute("relevamientoOficina", rel);

                try {
                    Long id = historialService.crearOficina(rel);
                    session.setAttribute("historialOficinaId", id);
                    session.setMaxInactiveInterval(604800); // 7 días
                    logger.info("Relevamiento oficina iniciado: " + nombre + " con ID: " + id);
                } catch (Exception ex) {
                    logger.severe("Error al crear relevamiento en BD: " + ex.getMessage());
                    model.addAttribute("error", "Error al inicializar el relevamiento");
                    return "index";
                }

                return "redirect:/oficinas/relevamiento";
            }

            // RELEVAMIENTO DE BIENES
            if (archivo == null || archivo.isEmpty()) {
                model.addAttribute("error", "Debe cargar un archivo Excel");
                return "index";
            }

            List<String> seriales = ExcelService.leerSeriales(archivo);
            if (seriales == null || seriales.isEmpty()) {
                model.addAttribute("error", "El archivo Excel no contiene datos válidos");
                return "index";
            }

            Relevamiento relevamiento = new Relevamiento();
            relevamiento.cargarSeriales(seriales);

            session.setAttribute("relevamientoBienes", relevamiento);
            session.setAttribute("relevamientoActivo", true);
            session.setAttribute("ultimoSerial", null);
            session.setMaxInactiveInterval(604800);

            try {
                Long id = historialService.crearBienes(nombre, relevamiento);
                session.setAttribute("historialBienesId", id);
                logger.info("Relevamiento bienes iniciado: " + nombre + " con ID: " + id);
            } catch (Exception ex) {
                logger.severe("Error al crear relevamiento en BD: " + ex.getMessage());
                model.addAttribute("error", "Error al inicializar el relevamiento");
                return "index";
            }

            return "redirect:/relevamiento";

        } catch (Exception ex) {
            logger.severe("Error en iniciarRelevamiento: " + ex.getMessage());
            model.addAttribute("error", "Error inesperado al iniciar relevamiento");
            return "index";
        }
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
        try {
            Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

            if (relevamiento == null) {
                response.put("error", "No hay relevamiento activo");
                return response;
            }

            if (serial == null || serial.trim().isEmpty()) {
                response.put("error", "Serial vacío");
                return response;
            }

            String serialNormalizado = serial.trim().toUpperCase();

            if ("encontrado".equals(accion)) {
                relevamiento.marcarComoEncontrado(serial);
                session.setAttribute("ultimoSerial", serial);
                autoguardarBienes(session, relevamiento);
                response.put("serialProcesado", serial);
                return response;
            }

            if ("noInventariado".equals(accion)) {
                relevamiento.agregarSobrante(serial);
                autoguardarBienes(session, relevamiento);
                response.put("serialProcesado", serial);
                return response;
            }

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
                autoguardarBienes(session, relevamiento);
                response.put("serialProcesado", serial);
            }

            session.setAttribute("ultimoSerial", serial);
            return response;

        } catch (Exception ex) {
            logger.severe("Error en agregarSerial: " + ex.getMessage());
            response.put("error", "Error procesando serial");
            return response;
        }
    }

    @PostMapping("/eliminar-serial")
    @ResponseBody
    public Map<String, String> eliminarSerial(
            @RequestParam String serial,
            HttpSession session
    ) {
        Map<String, String> response = new HashMap<>();
        try {
            Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

            if (relevamiento == null) {
                response.put("error", "No hay relevamiento activo");
                return response;
            }

            if (serial == null || serial.trim().isEmpty()) {
                response.put("error", "Serial vacío");
                return response;
            }

            relevamiento.eliminar(serial);
            autoguardarBienes(session, relevamiento);
            response.put("success", "true");
            return response;

        } catch (Exception ex) {
            logger.severe("Error en eliminarSerial: " + ex.getMessage());
            response.put("error", "Error eliminando serial");
            return response;
        }
    }

    @PostMapping("/finalizar")
    public String finalizarRelevamiento(HttpSession session, Model model) {
        try {
            String nombre = (String) session.getAttribute("nombreRelevamiento");
            Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

            if (relevamiento == null) {
                logger.warning("Intento de finalizar relevamiento nulo");
                return "redirect:/";
            }

            autoguardarBienes(session, relevamiento);

            model.addAttribute("nombreRelevamiento", nombre);
            model.addAttribute("esperados", relevamiento.getNumeroSerialEsperado().size());
            model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado().size());
            model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante().size());

            session.setAttribute("yaGuardado", true);

            logger.info("Relevamiento finalizado: " + nombre);
            return "finalizado";

        } catch (Exception ex) {
            logger.severe("Error finalizando relevamiento: " + ex.getMessage());
            model.addAttribute("error", "Error al finalizar");
            return "finalizado";
        }
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcelBienes(HttpSession session) {
        try {
            String nombre = (String) session.getAttribute("nombreRelevamiento");
            Relevamiento relevamiento = (Relevamiento) session.getAttribute("relevamientoBienes");

            if (relevamiento == null || nombre == null) {
                logger.warning("Intento de exportar con datos nulos");
                return ResponseEntity.status(404).build();
            }

            byte[] excel = ExportService.generarExcel(
                    nombre,
                    relevamiento.getNumeroSerialEsperado(),
                    relevamiento.getNumeroSerialEncontrado(),
                    relevamiento.getNumeroSerialSobrante()
            );

            if (excel == null) {
                logger.warning("Error generando Excel");
                return ResponseEntity.status(500).build();
            }

            String filename = ExportService.generarNombreArchivo(nombre, "xlsx");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excel);

        } catch (Exception e) {
            logger.severe("Error exportando Excel: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/oficinas/exportar/excel")
    public ResponseEntity<byte[]> exportarExcelOficinas(HttpSession session) {
        try {
            RelevamientoOficina rel = (RelevamientoOficina) session.getAttribute("relevamientoOficina");
            if (rel == null) {
                logger.warning("Intento de exportar oficinas con datos nulos");
                return ResponseEntity.status(404).build();
            }

            byte[] excel = ExportService.generarExcelOficinas(rel);
            if (excel == null) {
                logger.warning("Error generando Excel de oficinas");
                return ResponseEntity.status(500).build();
            }

            String filename = ExportService.generarNombreArchivo(rel.getNombre(), "xlsx");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excel);

        } catch (Exception e) {
            logger.severe("Error exportando Excel de oficinas: " + e.getMessage());
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

    @PostMapping("/enviar-excel-bienes")
    @ResponseBody
    public Map<String, String> enviarExcelBienes(
            @RequestParam String prefijo,
            HttpSession session
    ) {
        Map<String, String> res = new HashMap<>();
        try {
            String nombre = (String) session.getAttribute("nombreRelevamiento");
            Relevamiento rel = (Relevamiento) session.getAttribute("relevamientoBienes");

            if (rel == null || nombre == null) {
                res.put("error", "No hay relevamiento activo");
                return res;
            }

            byte[] excel = ExportService.generarExcel(
                    nombre,
                    rel.getNumeroSerialEsperado(),
                    rel.getNumeroSerialEncontrado(),
                    rel.getNumeroSerialSobrante()
            );

            if (excel == null) {
                res.put("error", "Error al generar el Excel");
                return res;
            }

            String destinatario = prefijo.trim() + "@justiciacordoba.gob.ar";
            String nombreArchivo = ExportService.generarNombreArchivo(nombre, "xlsx");
            mailService.enviarExcel(destinatario, nombre, excel, nombreArchivo);

            res.put("ok", "true");
            res.put("destinatario", destinatario);
            logger.info("Excel de bienes enviado a: " + destinatario);

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
            logger.severe("Error enviando Excel: " + e.getMessage());
            res.put("error", "No se pudo enviar: " + e.getMessage());
        }
        return res;
    }

    // ── HELPERS ──────────────────────────────────────────────

    private void autoguardarBienes(HttpSession session, Relevamiento rel) {
        try {
            Long id = (Long) session.getAttribute("historialBienesId");
            String nombre = (String) session.getAttribute("nombreRelevamiento");

            if (id != null && nombre != null) {
                historialService.actualizarBienes(id, nombre, rel);
                logger.fine("Autoguardado de bienes exitoso - ID: " + id);
            }
        } catch (Exception ex) {
            logger.severe("Error en autoguardado de bienes: " + ex.getMessage());
        }
    }

    private void limpiarSesion(HttpSession session) {
        session.removeAttribute("relevamientoBienes");
        session.removeAttribute("relevamientoOficina");
        session.removeAttribute("nombreRelevamiento");
        session.removeAttribute("ultimoSerial");
        session.removeAttribute("relevamientoActivo");
        session.removeAttribute("yaGuardado");
        session.removeAttribute("historialBienesId");
        session.removeAttribute("historialOficinaId");
    }
}