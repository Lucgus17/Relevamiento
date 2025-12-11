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

        model.addAttribute("todosLosSeriales", relevamiento.getNumeroSerialEsperado());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante());
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("nombreRelevamiento", nombre);

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

        model.addAttribute("todosLosSeriales", relevamiento.getNumeroSerialEsperado());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante());
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("nombreRelevamiento", session.getAttribute("nombreRelevamiento"));

        return "relevamiento";
    }

    // ------------------------ BORRAR SOBRANTE ------------------------
    @PostMapping("/borrar-sobrante")
    public String borrarSobrante(@RequestParam("serial") String serial) {
        relevamiento.eliminarSobrante(serial);
        return "redirect:/relevamiento";
    }

    // ------------------------ MOSTRAR ------------------------
    @GetMapping("/relevamiento")
    public String mostrar(Model model, HttpSession session) {

        model.addAttribute("todosLosSeriales", relevamiento.getNumeroSerialEsperado());
        model.addAttribute("encontrados", relevamiento.getNumeroSerialEncontrado());
        model.addAttribute("sobrantes", relevamiento.getNumeroSerialSobrante());
        model.addAttribute("conteos", relevamiento.contarNumerosSeriales());
        model.addAttribute("nombreRelevamiento", session.getAttribute("nombreRelevamiento"));

        return "relevamiento";
    }

    @GetMapping("/eliminar")
    public String eliminar(@RequestParam String serial) {
        relevamiento.eliminar(serial);
        return "redirect:/relevamiento";
    }

}
