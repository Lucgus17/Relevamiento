package org.example;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    public static List<String> leerSeriales(MultipartFile archivo) {
        List<String> seriales = new ArrayList<>();

        try (InputStream is = archivo.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0); // Primera hoja

            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(3); // cuarta columna
                    if (cell != null) {

                        String valor = formatter
                                .formatCellValue(cell)
                                .trim();

                        if (!valor.isEmpty()) {
                            seriales.add(valor);
                        }
                    }
                }
            }

            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return seriales;
    }

    /**
     * NUEVO formato de importación para el relevamiento de oficinas.
     *
     * Todo va en la PRIMERA COLUMNA (columna A) del Excel:
     *  - La primera fila (encabezado) se descarta.
     *  - Una celda en NEGRITA representa el nombre de una oficina.
     *  - Las celdas siguientes, SIN negrita, son los empleados de esa
     *    oficina, hasta que aparece la próxima celda en negrita
     *    (la siguiente oficina).
     *
     * Ejemplo de columna A:
     *   Fila 1: (encabezado, se ignora)
     *   Fila 2: OFICINA CENTRAL        <- negrita
     *   Fila 3: Juan Pérez
     *   Fila 4: María García
     *   Fila 5: OFICINA NORTE          <- negrita
     *   Fila 6: Pedro López
     */
    public static List<Oficina> leerOficinasDesdeExcel(MultipartFile archivo) {
        List<Oficina> oficinas = new ArrayList<>();

        try (InputStream is = archivo.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Oficina oficinaActual = null;

            // arranca en 1 para saltear la fila de encabezado
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cell = row.getCell(0);
                if (cell == null) continue;

                String valor = formatter.formatCellValue(cell).trim();
                if (valor.isEmpty()) continue;

                if (esCeldaNegrita(workbook, cell)) {
                    oficinaActual = new Oficina(valor);
                    oficinas.add(oficinaActual);
                } else if (oficinaActual != null) {
                    oficinaActual.getEmpleados().add(new Empleado(valor, null));
                }
                // si aparece un nombre antes de la primera oficina (oficinaActual == null),
                // se ignora porque no sabemos a qué oficina pertenece.
            }

            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return oficinas;
    }

    private static boolean esCeldaNegrita(Workbook workbook, Cell cell) {
        try {
            CellStyle estilo = cell.getCellStyle();
            if (estilo == null) return false;
            Font font = workbook.getFontAt(estilo.getFontIndexAsInt());
            return font != null && font.getBold();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Se mantiene por compatibilidad con el formato viejo (columna A = nombre,
     * columna B = cargo), por si todavía se usa en algún otro lado.
     */
    public static List<Empleado> leerEmpleadosDesdeExcel(MultipartFile archivo) {

        List<Empleado> empleados = new ArrayList<>();

        try (InputStream is = archivo.getInputStream()) {

            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0); // hoja 1

            DataFormatter formatter = new DataFormatter();


            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nombre = formatter
                        .formatCellValue(row.getCell(0))
                        .trim();

                String cargo = formatter
                        .formatCellValue(row.getCell(1))
                        .trim();

                if (!nombre.isEmpty()) {
                    empleados.add(new Empleado(nombre, cargo));
                }
            }

            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return empleados;
    }

}