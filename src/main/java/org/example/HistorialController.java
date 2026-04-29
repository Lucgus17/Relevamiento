package org.example;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/historial")
public class HistorialController {

    @Autowired
    private HistorialService historialService;

    @GetMapping
    public String historial(Model model) {
        model.addAttribute("bienes", historialService.listarBienes());
        model.addAttribute("oficinas", historialService.listarOficinas());
        return "historial";
    }

    @GetMapping("/bienes/{id}/excel")
    public ResponseEntity<byte[]> exportarBienes(@PathVariable Long id) {
        return historialService.findBienes(id).map(e -> {
            byte[] excel = ExportService.generarExcel(
                    e.getNombre(), e.getEsperados(), e.getEncontrados(), e.getSobrantes());
            if (excel == null) return ResponseEntity.status(500).<byte[]>build();
            String filename = ExportService.generarNombreArchivo(e.getNombre(), "xlsx");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excel);
        }).orElse(ResponseEntity.notFound().<byte[]>build());
    }

    @GetMapping("/oficinas/{id}/excel")
    public ResponseEntity<byte[]> exportarOficina(@PathVariable Long id) {
        return historialService.findOficina(id).map(e -> {
            RelevamientoOficina rel = historialService.toRelevamientoOficina(e);
            byte[] excel = ExportService.generarExcelOficinas(rel);
            if (excel == null) return ResponseEntity.status(500).<byte[]>build();
            String filename = ExportService.generarNombreArchivo(e.getNombre(), "xlsx");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excel);
        }).orElse(ResponseEntity.notFound().<byte[]>build());
    }

    /** Retomar un relevamiento de bienes desde el historial */
    @GetMapping("/bienes/{id}/retomar")
    public String retomarBienes(@PathVariable Long id, HttpSession session) {
        return historialService.findBienes(id).map(e -> {
            Relevamiento rel = historialService.toBienes(e);
            session.setAttribute("relevamientoBienes", rel);
            session.setAttribute("nombreRelevamiento", e.getNombre());
            session.setAttribute("relevamientoActivo", true);
            session.setAttribute("ultimoSerial", null);
            session.setAttribute("historialBienesId", e.getId());
            session.removeAttribute("yaGuardado");
            return "redirect:/relevamiento";
        }).orElse("redirect:/historial");
    }

    /** Retomar un relevamiento de oficinas desde el historial */
    @GetMapping("/oficinas/{id}/retomar")
    public String retomarOficina(@PathVariable Long id, HttpSession session) {
        return historialService.findOficina(id).map(e -> {
            RelevamientoOficina rel = historialService.toRelevamientoOficina(e);
            session.setAttribute("relevamientoOficina", rel);
            session.setAttribute("nombreRelevamiento", e.getNombre());
            session.setAttribute("historialOficinaId", e.getId());
            session.removeAttribute("yaGuardado");
            return "redirect:/oficinas/relevamiento";
        }).orElse("redirect:/historial");
    }
}