package org.example;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class HomeController {

    private final Relevamiento relevamiento = new Relevamiento();

    // -------- Página de inicio --------
    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {
        model.addAttribute("inicioUltimaSesion", session.getAttribute("inicioUltimaSesion"));
        model.addAttribute("finUltimaSesion", session.getAttribute("finUltimaSesion"));
        return "index";
    }

    // -------- Iniciar relevamiento --------
    @PostMapping("/iniciar-relevamiento")
    public String iniciarRelevamiento(
            @RequestParam("nombreRelevamiento") String nombre,
            @RequestParam("archivo") MultipartFile archivo,
            Model model,
            HttpSession session
    ) {
        List<String> seriales = ExcelService.leerSeriales(archivo);
        relevamiento.cargarSeriales(seriales);

        session.setAttribute("relevamientoActivo", true);
        session.setAttribute("nombreRelevamiento", nombre);
        session.setAttribute("inicioUltimaSesion", java.time.LocalDateTime.now());

        model.addAttribute("todosLosSeriales", String.join("\n", relevamiento.getNumeroSerialEsperado()));
        model.addAttribute("encontrados", String.join("\n", relevamiento.getNumeroSerialEncontrado()));
        model.addAttribute("sobrantes", String.join("\n", relevamiento.getNumeroSerialSobrante()));
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("inicio", java.time.LocalDateTime.now());
        model.addAttribute("nombreRelevamiento", nombre);

        return "relevamiento";
    }

    // -------- Agregar serial con sugerencia --------
    @PostMapping("/agregar-serial")
    public String agregarSerial(@RequestParam("serial") String serial,
                                @RequestParam(value="accion", required=false) String accion,
                                Model model, HttpSession session) {

        Boolean activo = (Boolean) session.getAttribute("relevamientoActivo");
        if (activo == null || !activo) return "redirect:/";

        if ("noInventariado".equals(accion)) {
            relevamiento.agregarSobrante(serial);
        } else {
            // acción normal: procesar con sugerencia
            String sugerencia = relevamiento.procesarInputConSugerencia(serial);
            if (sugerencia != null) {
                model.addAttribute("sugerencia", sugerencia);
                model.addAttribute("serialOriginal", serial);
            }
        }

        model.addAttribute("todosLosSeriales", String.join("\n", relevamiento.getNumeroSerialEsperado()));
        model.addAttribute("encontrados", String.join("\n", relevamiento.getNumeroSerialEncontrado()));
        model.addAttribute("sobrantes", String.join("\n", relevamiento.getNumeroSerialSobrante()));
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("inicio", java.time.LocalDateTime.now());
        model.addAttribute("nombreRelevamiento", session.getAttribute("nombreRelevamiento"));

        return "relevamiento";
    }

    // -------- Mostrar relevamiento actual --------
    @GetMapping("/relevamiento")
    public String mostrarRelevamiento(Model model, HttpSession session) {
        String nombre = (String) session.getAttribute("nombreRelevamiento");
        if (nombre == null) {
            return "redirect:/";
        }

        model.addAttribute("todosLosSeriales", String.join("\n", relevamiento.getNumeroSerialEsperado()));
        model.addAttribute("encontrados", String.join("\n", relevamiento.getNumeroSerialEncontrado()));
        model.addAttribute("sobrantes", String.join("\n", relevamiento.getNumeroSerialSobrante()));
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("inicio", java.time.LocalDateTime.now());
        model.addAttribute("nombreRelevamiento", nombre);

        return "relevamiento";
    }

    // -------- Finalizar sesión --------
    @PostMapping("/finalizar")
    public String finalizarSesion(Model model, HttpSession session) {
        String nombre = (String) session.getAttribute("nombreRelevamiento");
        session.setAttribute("relevamientoActivo", false);
        session.setAttribute("finUltimaSesion", java.time.LocalDateTime.now());

        model.addAttribute("esperados", relevamiento.getNumeroSerialEsperado().size());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado().size());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante().size());
        model.addAttribute("nombreRelevamiento", nombre);

        return "finalizado";
    }

    // -------- Exportar Excel --------
    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel(HttpSession session) {
        String nombre = (String) session.getAttribute("nombreRelevamiento");
        if (nombre == null) {
            return ResponseEntity.status(302).header("Location", "/").build();
        }

        byte[] bytes = ExportService.generarExcel(
                nombre,
                relevamiento.getNumeroSerialEsperado(),
                relevamiento.getNumeroSerialEncontrado(),
                relevamiento.getNumeroSerialSobrante()
        );

        String nombreArchivo = ExportService.generarNombreArchivo(nombre, "xlsx");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // -------- Exportar PDF --------
    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarPdf(HttpSession session) {
        String nombre = (String) session.getAttribute("nombreRelevamiento");
        if (nombre == null) {
            return ResponseEntity.status(302).header("Location", "/").build();
        }

        byte[] bytes = ExportService.generarPdf(
                nombre,
                relevamiento.getNumeroSerialEsperado(),
                relevamiento.getNumeroSerialEncontrado(),
                relevamiento.getNumeroSerialSobrante()
        );

        String nombreArchivo = ExportService.generarNombreArchivo(nombre, "pdf");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
