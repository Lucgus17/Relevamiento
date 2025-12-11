package org.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
public class HomeController {

    private final Relevamiento relevamiento = new Relevamiento();

    // ------------------------ INICIO ------------------------
    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    // ------------------------ INICIAR RELEVAMIENTO ------------------------
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
        session.setAttribute("ultimoSerial", null);

        model.addAttribute("todosLosSeriales", relevamiento.getNumeroSerialEsperado());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante());
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("nombreRelevamiento", nombre);
        model.addAttribute("ultimoSerial", null);

        return "relevamiento";
    }

    // ------------------------ AGREGAR SERIAL ------------------------
    @PostMapping("/agregar-serial")
    public String agregarSerial(
            @RequestParam("serial") String serial,
            @RequestParam(value = "accion", required = false) String accion,
            Model model,
            HttpSession session
    ) {
        if ("noInventariado".equals(accion)) {
            relevamiento.agregarSobrante(serial);
        } else {
            String sugerencia = relevamiento.procesarInputConSugerencia(serial);
            if (sugerencia != null) {
                model.addAttribute("sugerencia", sugerencia);
                model.addAttribute("serialOriginal", serial);
            }
        }

        session.setAttribute("ultimoSerial", serial);

        model.addAttribute("todosLosSeriales", relevamiento.getNumeroSerialEsperado());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante());
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("nombreRelevamiento", session.getAttribute("nombreRelevamiento"));
        model.addAttribute("ultimoSerial", serial);

        return "relevamiento";
    }

    // ------------------------ ELIMINAR SERIAL ------------------------
    @GetMapping("/eliminar")
    public String eliminar(@RequestParam String serial) {
        relevamiento.eliminar(serial);
        return "redirect:/relevamiento";
    }

    // ------------------------ MOSTRAR RELEVAMIENTO ------------------------
    @GetMapping("/relevamiento")
    public String mostrar(Model model, HttpSession session) {
        model.addAttribute("todosLosSeriales", relevamiento.getNumeroSerialEsperado());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante());
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("nombreRelevamiento", session.getAttribute("nombreRelevamiento"));
        model.addAttribute("ultimoSerial", session.getAttribute("ultimoSerial"));

        return "relevamiento";
    }

    // ------------------------ FINALIZAR SESIÓN ------------------------
    @PostMapping("/finalizar")
    public String finalizarSesion(HttpSession session, Model model) {
        model.addAttribute("esperados", relevamiento.getNumeroSerialEsperado().size());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado().size());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante().size());

        session.invalidate();
        return "finalizado";
    }

    // ------------------------ EXPORTAR EXCEL ------------------------
    @GetMapping("/exportar-excel")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" +
                ExportService.generarNombreArchivo("relevamiento", "xlsx"));

        byte[] archivo = ExportService.generarExcel(
                "Relevamiento",
                relevamiento.getNumeroSerialEsperado(),
                relevamiento.getNumeroSerialEncontrado(),
                relevamiento.getNumeroSerialSobrante()
        );

        response.getOutputStream().write(archivo);
        response.getOutputStream().flush();
    }

    // ------------------------ EXPORTAR PDF ------------------------
    @GetMapping("/exportar-pdf")
    public void exportarPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" +
                ExportService.generarNombreArchivo("relevamiento", "pdf"));

        byte[] archivo = ExportService.generarPdf(
                "Relevamiento",
                relevamiento.getNumeroSerialEsperado(),
                relevamiento.getNumeroSerialEncontrado(),
                relevamiento.getNumeroSerialSobrante()
        );

        response.getOutputStream().write(archivo);
        response.getOutputStream().flush();
    }
}
