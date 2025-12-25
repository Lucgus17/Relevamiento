package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExportService {

    public static String generarNombreArchivo(String nombre, String extension) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm"));
        return nombre + " (" + fecha + ")." + extension;
    }

    // =====================================================================
    // EXPORTAR EXCEL - BIENES (Seriales)
    // =====================================================================
    public static byte[] generarExcel(
            String nombre,
            List<String> esperados,
            List<String> encontrados,
            List<String> sobrantes
    ) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Relevamiento");

            // ================= ESTILOS =================
            Font tituloFont = wb.createFont();
            tituloFont.setBold(true);
            tituloFont.setFontHeightInPoints((short) 16);

            CellStyle tituloStyle = wb.createCellStyle();
            tituloStyle.setFont(tituloFont);

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // ================= TITULO =================
            Row tituloRow = sheet.createRow(0);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue(nombre);
            tituloCell.setCellStyle(tituloStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

            // ================= FECHA =================
            Row fechaRow = sheet.createRow(1);
            fechaRow.createCell(0).setCellValue(
                    "Fecha fin relevamiento: " + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 2));

            // ================= RESUMEN =================
            Row resumenRow = sheet.createRow(2);
            resumenRow.createCell(0).setCellValue(
                    String.format("Esperados: %d  |  Encontrados: %d  |  No inventariados: %d",
                            esperados.size(), encontrados.size(), sobrantes.size())
            );
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 0, 2));

            // ================= HEADERS =================
            Row header = sheet.createRow(4);
            String[] columnas = {"ESPERADOS", "ENCONTRADOS", "NO INVENTARIADOS"};

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // ================= DATA =================
            int maxRows = Math.max(esperados.size(),
                    Math.max(encontrados.size(), sobrantes.size()));

            for (int i = 0; i < maxRows; i++) {
                Row row = sheet.createRow(i + 5);

                if (i < esperados.size()) {
                    Cell c = row.createCell(0);
                    c.setCellValue(esperados.get(i));
                    c.setCellStyle(cellStyle);
                }
                if (i < encontrados.size()) {
                    Cell c = row.createCell(1);
                    c.setCellValue(encontrados.get(i));
                    c.setCellStyle(cellStyle);
                }
                if (i < sobrantes.size()) {
                    Cell c = row.createCell(2);
                    c.setCellValue(sobrantes.get(i));
                    c.setCellStyle(cellStyle);
                }
            }

            // Ajustar columnas
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
                // Agregar un poco más de espacio
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =====================================================================
    // EXPORTAR EXCEL - OFICINAS (Empleados y Equipos)
    // =====================================================================
    public static byte[] generarExcelOficinas(RelevamientoOficina rel) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ================= ESTILOS =================
            Font tituloFont = wb.createFont();
            tituloFont.setBold(true);
            tituloFont.setFontHeightInPoints((short) 16);

            CellStyle tituloStyle = wb.createCellStyle();
            tituloStyle.setFont(tituloFont);

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyle.setWrapText(true); // ← IMPORTANTE: Permitir múltiples líneas

            CellStyle empleadoStyle = wb.createCellStyle();
            empleadoStyle.cloneStyleFrom(cellStyle);
            Font empleadoFont = wb.createFont();
            empleadoFont.setBold(true);
            empleadoStyle.setFont(empleadoFont);
            empleadoStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            empleadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ================= HOJA 1: EMPLEADOS Y SUS EQUIPOS =================
            Sheet sheet1 = wb.createSheet("Empleados");

            // Título
            Row titulo1 = sheet1.createRow(0);
            Cell tituloCell1 = titulo1.createCell(0);
            tituloCell1.setCellValue(rel.getNombre() + " - Empleados");
            tituloCell1.setCellStyle(tituloStyle);
            sheet1.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

            // Fecha
            Row fecha1 = sheet1.createRow(1);
            fecha1.createCell(0).setCellValue(
                    "Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            sheet1.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 6));

            // Headers
            Row header1 = sheet1.createRow(3);
            String[] columnas1 = {"EMPLEADO", "CPU", "MONITOR", "TELÉFONO IP", "CÁMARA", "FIRMA DIGITAL", "LECTOR ÓPTICO"};
            for (int i = 0; i < columnas1.length; i++) {
                Cell cell = header1.createCell(i);
                cell.setCellValue(columnas1[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data - Empleados
            // Data - Empleados
            int rowNum = 4;
            for (Empleado emp : rel.getEmpleados()) {
                Row row = sheet1.createRow(rowNum++);

                // Nombre del empleado con estilo especial
                Cell cellNombre = row.createCell(0);
                cellNombre.setCellValue(emp.getNombre());
                cellNombre.setCellStyle(empleadoStyle);

                // CPU (con nombre si existe)
                List<EquipoUsuario> cpus = emp.getEquipos().stream()
                        .filter(e -> "CPU".equalsIgnoreCase(e.getTipo()))
                        .collect(java.util.stream.Collectors.toList());

                String cpu = "";
                if (cpus.size() == 1) {
                    EquipoUsuario c = cpus.get(0);
                    cpu = c.getNumeroSerie();
                    if (c.getNombre() != null && !c.getNombre().isEmpty()) {
                        cpu += " (" + c.getNombre() + ")";
                    }
                } else if (cpus.size() > 1) {
                    for (int i = 0; i < cpus.size(); i++) {
                        EquipoUsuario c = cpus.get(i);
                        cpu += "CPU" + (i + 1) + ": " + c.getNumeroSerie();
                        if (c.getNombre() != null && !c.getNombre().isEmpty()) {
                            cpu += " (" + c.getNombre() + ")";
                        }
                        if (i < cpus.size() - 1) cpu += "\n";
                    }
                }
                Cell cellCPU = row.createCell(1);
                cellCPU.setCellValue(cpu);
                cellCPU.setCellStyle(cellStyle);

                // Monitor
                List<EquipoUsuario> monitores = emp.getEquipos().stream()
                        .filter(e -> "Monitor".equalsIgnoreCase(e.getTipo()))
                        .collect(java.util.stream.Collectors.toList());

                String monitor = "";
                if (monitores.size() == 1) {
                    monitor = monitores.get(0).getNumeroSerie();
                } else if (monitores.size() > 1) {
                    for (int i = 0; i < monitores.size(); i++) {
                        monitor += "Monitor" + (i + 1) + ": " + monitores.get(i).getNumeroSerie();
                        if (i < monitores.size() - 1) monitor += "\n";
                    }
                }
                Cell cellMonitor = row.createCell(2);
                cellMonitor.setCellValue(monitor);
                cellMonitor.setCellStyle(cellStyle);

                // Teléfono IP
                List<EquipoUsuario> telefonos = emp.getEquipos().stream()
                        .filter(e -> "Teléfono IP".equalsIgnoreCase(e.getTipo()))
                        .collect(java.util.stream.Collectors.toList());

                String telefono = "";
                if (telefonos.size() == 1) {
                    telefono = telefonos.get(0).getNumeroSerie();
                } else if (telefonos.size() > 1) {
                    for (int i = 0; i < telefonos.size(); i++) {
                        telefono += "Teléfono" + (i + 1) + ": " + telefonos.get(i).getNumeroSerie();
                        if (i < telefonos.size() - 1) telefono += "\n";
                    }
                }
                Cell cellTelefono = row.createCell(3);
                cellTelefono.setCellValue(telefono);
                cellTelefono.setCellStyle(cellStyle);

                // Cámara
                List<EquipoUsuario> camaras = emp.getEquipos().stream()
                        .filter(e -> "Cámara".equalsIgnoreCase(e.getTipo()))
                        .collect(java.util.stream.Collectors.toList());

                String camara = "";
                if (camaras.size() == 1) {
                    camara = camaras.get(0).getNumeroSerie();
                } else if (camaras.size() > 1) {
                    for (int i = 0; i < camaras.size(); i++) {
                        camara += "Cámara" + (i + 1) + ": " + camaras.get(i).getNumeroSerie();
                        if (i < camaras.size() - 1) camara += "\n";
                    }
                }
                Cell cellCamara = row.createCell(4);
                cellCamara.setCellValue(camara);
                cellCamara.setCellStyle(cellStyle);

                // Firma digital
                List<EquipoUsuario> firmas = emp.getEquipos().stream()
                        .filter(e -> "Firma digital".equalsIgnoreCase(e.getTipo()))
                        .collect(java.util.stream.Collectors.toList());

                String firma = "";
                if (firmas.size() == 1) {
                    firma = firmas.get(0).getNumeroSerie();
                } else if (firmas.size() > 1) {
                    for (int i = 0; i < firmas.size(); i++) {
                        firma += "Firma" + (i + 1) + ": " + firmas.get(i).getNumeroSerie();
                        if (i < firmas.size() - 1) firma += "\n";
                    }
                }
                Cell cellFirma = row.createCell(5);
                cellFirma.setCellValue(firma);
                cellFirma.setCellStyle(cellStyle);

                // Lector Optico
                List<EquipoUsuario> lectores = emp.getEquipos().stream()
                        .filter(e -> "Lector Optico".equalsIgnoreCase(e.getTipo()))
                        .collect(java.util.stream.Collectors.toList());

                String lector = "";
                if (lectores.size() == 1) {
                    lector = lectores.get(0).getNumeroSerie();
                } else if (lectores.size() > 1) {
                    for (int i = 0; i < lectores.size(); i++) {
                        lector += "Lector" + (i + 1) + ": " + lectores.get(i).getNumeroSerie();
                        if (i < lectores.size() - 1) lector += "\n";
                    }
                }
                Cell cellLectorOptico = row.createCell(6);
                cellLectorOptico.setCellValue(lector);
                cellLectorOptico.setCellStyle(cellStyle);
            }

            // Ajustar columnas (CORREGIDO: 7 columnas en total)
            for (int i = 0; i < 7; i++) {
                sheet1.autoSizeColumn(i);
                sheet1.setColumnWidth(i, sheet1.getColumnWidth(i) + 1000);
            }

            // ================= HOJA 2: EQUIPOS DE OFICINA =================
            Sheet sheet2 = wb.createSheet("Equipos de Oficina");

            // Título
            Row titulo2 = sheet2.createRow(0);
            Cell tituloCell2 = titulo2.createCell(0);
            tituloCell2.setCellValue(rel.getNombre() + " - Equipos de Oficina");
            tituloCell2.setCellStyle(tituloStyle);
            sheet2.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

            // Fecha
            Row fecha2 = sheet2.createRow(1);
            fecha2.createCell(0).setCellValue(
                    "Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            sheet2.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 2));

            // Headers
            Row header2 = sheet2.createRow(3);
            String[] columnas2 = {"TIPO", "NÚMERO DE SERIE", "NOMBRE"};
            for (int i = 0; i < columnas2.length; i++) {
                Cell cell = header2.createCell(i);
                cell.setCellValue(columnas2[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data - Equipos de Oficina
            rowNum = 4;
            for (EquipoOficina eq : rel.getEquiposOficina()) {
                Row row = sheet2.createRow(rowNum++);

                Cell cellTipo = row.createCell(0);
                cellTipo.setCellValue(eq.getTipo());
                cellTipo.setCellStyle(cellStyle);

                Cell cellSN = row.createCell(1);
                cellSN.setCellValue(eq.getNumeroSerie());
                cellSN.setCellStyle(cellStyle);

                Cell cellNombre = row.createCell(2);
                cellNombre.setCellValue(eq.getNombre() != null ? eq.getNombre() : "");
                cellNombre.setCellStyle(cellStyle);
            }

            // Ajustar columnas
            for (int i = 0; i < 3; i++) {
                sheet2.autoSizeColumn(i);
                sheet2.setColumnWidth(i, sheet2.getColumnWidth(i) + 1000);
            }

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =====================================================================
    // UTILIDAD - Leer empleados desde Excel (placeholder)
    // =====================================================================
    public static List<Empleado> leerEmpleadosDesdeExcel(MultipartFile archivo) {
        List<Empleado> empleados = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Saltar header (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cellNombre = row.getCell(0);
                if (cellNombre == null) continue;

                String nombre = cellNombre.getStringCellValue();
                if (nombre != null && !nombre.trim().isEmpty()) {
                    Empleado emp = new Empleado();
                    emp.setNombre(nombre.trim());
                    empleados.add(emp);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return empleados;
    }
}