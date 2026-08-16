package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;




public class ExportService {

    public static String generarNombreArchivo(String nombre, String extension) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm"));
        return nombre + " (" + fecha + ")." + extension;
    }

    private static final byte[] SLATE_800   = { (byte)0x1E, (byte)0x29, (byte)0x3B };
    private static final byte[] SLATE_600   = { (byte)0x47, (byte)0x55, (byte)0x69 };
    private static final byte[] SLATE_200   = { (byte)0xE2, (byte)0xE8, (byte)0xF0 };
    private static final byte[] SLATE_50    = { (byte)0xF8, (byte)0xFA, (byte)0xFC };
    private static final byte[] WHITE       = { (byte)0xFF, (byte)0xFF, (byte)0xFF };
    private static final byte[] GREEN_700   = { (byte)0x15, (byte)0x80, (byte)0x3D };
    private static final byte[] GREEN_50    = { (byte)0xF0, (byte)0xFD, (byte)0xF4 };
    private static final byte[] GREEN_200   = { (byte)0xBB, (byte)0xF7, (byte)0xD0 };
    private static final byte[] AMBER_900   = { (byte)0x78, (byte)0x35, (byte)0x00 };
    private static final byte[] AMBER_100   = { (byte)0xFE, (byte)0xF3, (byte)0xC7 };
    private static final byte[] AMBER_300   = { (byte)0xFC, (byte)0xD3, (byte)0x4D };

    private static XSSFColor xc(byte[] rgb) {
        return new XSSFColor(rgb, new DefaultIndexedColorMap());
    }

    private static void bordes(XSSFCellStyle s, byte[] color) {
        XSSFColor c = xc(color);
        s.setBorderTop(BorderStyle.THIN);    s.setTopBorderColor(c);
        s.setBorderBottom(BorderStyle.THIN); s.setBottomBorderColor(c);
        s.setBorderLeft(BorderStyle.THIN);   s.setLeftBorderColor(c);
        s.setBorderRight(BorderStyle.THIN);  s.setRightBorderColor(c);
    }

    /** Sin cambios respecto al original: exporta esperados/encontrados/sobrantes. */
    public static byte[] generarExcel(
            String nombre,
            List<String> esperados,
            List<String> encontrados,
            List<String> sobrantes
    ) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Relevamiento");
            sheet.setDefaultRowHeightInPoints(16f);

            XSSFFont fTitulo = wb.createFont();
            fTitulo.setFontName("Calibri"); fTitulo.setBold(true);
            fTitulo.setFontHeightInPoints((short) 16); fTitulo.setColor(xc(SLATE_800));

            XSSFFont fMeta = wb.createFont();
            fMeta.setFontName("Calibri"); fMeta.setItalic(true);
            fMeta.setFontHeightInPoints((short) 9); fMeta.setColor(xc(SLATE_600));

            XSSFFont fHeader = wb.createFont();
            fHeader.setFontName("Calibri"); fHeader.setBold(true);
            fHeader.setFontHeightInPoints((short) 10); fHeader.setColor(xc(WHITE));

            XSSFFont fCelda = wb.createFont();
            fCelda.setFontName("Calibri"); fCelda.setFontHeightInPoints((short) 10);
            fCelda.setColor(xc(SLATE_800));

            XSSFCellStyle sTitulo = wb.createCellStyle();
            sTitulo.setFont(fTitulo); sTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
            sTitulo.setFillForegroundColor(xc(WHITE)); sTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle sMeta = wb.createCellStyle();
            sMeta.setFont(fMeta);
            sMeta.setFillForegroundColor(xc(WHITE)); sMeta.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle sHeader = wb.createCellStyle();
            sHeader.setFont(fHeader); sHeader.setAlignment(HorizontalAlignment.CENTER);
            sHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            sHeader.setFillForegroundColor(xc(SLATE_800)); sHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(sHeader, SLATE_600);

            XSSFCellStyle sCelda = wb.createCellStyle();
            sCelda.setFont(fCelda); sCelda.setVerticalAlignment(VerticalAlignment.CENTER);
            sCelda.setFillForegroundColor(xc(WHITE)); sCelda.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(sCelda, SLATE_200);

            XSSFCellStyle sCeldaZ = wb.createCellStyle();
            sCeldaZ.setFont(fCelda); sCeldaZ.setVerticalAlignment(VerticalAlignment.CENTER);
            sCeldaZ.setFillForegroundColor(xc(SLATE_50)); sCeldaZ.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(sCeldaZ, SLATE_200);

            Row rT = sheet.createRow(0); rT.setHeightInPoints(28);
            Cell cT = rT.createCell(0); cT.setCellValue(nombre); cT.setCellStyle(sTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            Row rF = sheet.createRow(1); rF.setHeightInPoints(15);
            Cell cF = rF.createCell(0);
            cF.setCellValue("Fecha fin: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            cF.setCellStyle(sMeta);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));

            Row rR = sheet.createRow(2); rR.setHeightInPoints(15);
            Cell cR = rR.createCell(0);
            cR.setCellValue(String.format("Esperados: %d   ·   Encontrados: %d   ·   No inventariados: %d",
                    esperados.size(), encontrados.size(), sobrantes.size()));
            cR.setCellStyle(sMeta);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 2));

            sheet.createRow(3);

            Row rH = sheet.createRow(4); rH.setHeightInPoints(20);
            String[] cols = {"ESPERADOS", "ENCONTRADOS", "NO INVENTARIADOS"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = rH.createCell(i); cell.setCellValue(cols[i]); cell.setCellStyle(sHeader);
            }

            int max = Math.max(esperados.size(), Math.max(encontrados.size(), sobrantes.size()));
            for (int i = 0; i < max; i++) {
                Row row = sheet.createRow(i + 5); row.setHeight((short)-1);
                XSSFCellStyle est = (i % 2 == 0) ? sCelda : sCeldaZ;
                if (i < esperados.size())   { Cell c = row.createCell(0); c.setCellValue(esperados.get(i));   c.setCellStyle(est); }
                if (i < encontrados.size()) { Cell c = row.createCell(1); c.setCellValue(encontrados.get(i)); c.setCellStyle(est); }
                if (i < sobrantes.size())   { Cell c = row.createCell(2); c.setCellValue(sobrantes.get(i));   c.setCellStyle(est); }
            }

            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1200);
            }

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    /**
     * Exporta el relevamiento de oficinas.
     *
     * CAMBIOS respecto a la versión anterior:
     *  - Se agrega la columna OFICINA (tanto en la tabla de empleados como
     *    en la de equipamiento de oficina) para saber a qué oficina
     *    pertenece cada fila.
     *  - El número de serie y el nombre de cada equipo del empleado ya NO
     *    van juntos en la misma celda: cada tipo de equipo tiene ahora dos
     *    columnas, "... (N/S)" y "... (Nombre)".
     *
     * Requiere que RelevamientoOficina exponga getOficinas() -> List<Oficina>,
     * y que cada Oficina exponga getNombre(), getEmpleados() y getEquiposOficina().
     */
    public static byte[] generarExcelOficinas(RelevamientoOficina rel) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Relevamiento");
            sheet.setDefaultRowHeightInPoints(16f);

            XSSFFont fTitulo = wb.createFont();
            fTitulo.setFontName("Calibri"); fTitulo.setBold(true);
            fTitulo.setFontHeightInPoints((short) 16); fTitulo.setColor(xc(SLATE_800));

            XSSFFont fMeta = wb.createFont();
            fMeta.setFontName("Calibri"); fMeta.setItalic(true);
            fMeta.setFontHeightInPoints((short) 9); fMeta.setColor(xc(SLATE_600));

            XSSFFont fOficinaTitulo = wb.createFont();
            fOficinaTitulo.setFontName("Calibri"); fOficinaTitulo.setBold(true);
            fOficinaTitulo.setFontHeightInPoints((short) 13); fOficinaTitulo.setColor(xc(WHITE));

            XSSFFont fSubSeccion = wb.createFont();
            fSubSeccion.setFontName("Calibri"); fSubSeccion.setBold(true); fSubSeccion.setItalic(true);
            fSubSeccion.setFontHeightInPoints((short) 9); fSubSeccion.setColor(xc(GREEN_700));

            XSSFFont fHeader = wb.createFont();
            fHeader.setFontName("Calibri"); fHeader.setBold(true);
            fHeader.setFontHeightInPoints((short) 10); fHeader.setColor(xc(WHITE));

            XSSFFont fEmpleado = wb.createFont();
            fEmpleado.setFontName("Calibri"); fEmpleado.setBold(true);
            fEmpleado.setFontHeightInPoints((short) 10); fEmpleado.setColor(xc(SLATE_800));

            XSSFFont fCelda = wb.createFont();
            fCelda.setFontName("Calibri"); fCelda.setFontHeightInPoints((short) 10);
            fCelda.setColor(xc(SLATE_800));

            XSSFFont fComentario = wb.createFont();
            fComentario.setFontName("Calibri"); fComentario.setItalic(true);
            fComentario.setFontHeightInPoints((short) 10); fComentario.setColor(xc(AMBER_900));

            XSSFCellStyle sTitulo = wb.createCellStyle();
            sTitulo.setFont(fTitulo); sTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
            sTitulo.setFillForegroundColor(xc(WHITE)); sTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle sMeta = wb.createCellStyle();
            sMeta.setFont(fMeta);
            sMeta.setFillForegroundColor(xc(WHITE)); sMeta.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle sOficinaTitulo = wb.createCellStyle();
            sOficinaTitulo.setFont(fOficinaTitulo); sOficinaTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
            sOficinaTitulo.setIndention((short) 1);
            sOficinaTitulo.setFillForegroundColor(xc(GREEN_700)); sOficinaTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle sSubSeccion = wb.createCellStyle();
            sSubSeccion.setFont(fSubSeccion); sSubSeccion.setVerticalAlignment(VerticalAlignment.CENTER);
            sSubSeccion.setIndention((short) 1);
            sSubSeccion.setFillForegroundColor(xc(GREEN_50)); sSubSeccion.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle sHeader = wb.createCellStyle();
            sHeader.setFont(fHeader); sHeader.setAlignment(HorizontalAlignment.CENTER);
            sHeader.setVerticalAlignment(VerticalAlignment.CENTER); sHeader.setWrapText(true);
            sHeader.setFillForegroundColor(xc(SLATE_800)); sHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(sHeader, SLATE_600);

            XSSFCellStyle sEmpleado = wb.createCellStyle();
            sEmpleado.setFont(fEmpleado); sEmpleado.setVerticalAlignment(VerticalAlignment.CENTER);
            sEmpleado.setWrapText(true);
            sEmpleado.setFillForegroundColor(xc(SLATE_50)); sEmpleado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(sEmpleado, SLATE_200);

            XSSFCellStyle sCelda = wb.createCellStyle();
            sCelda.setFont(fCelda); sCelda.setVerticalAlignment(VerticalAlignment.CENTER); sCelda.setWrapText(true);
            sCelda.setFillForegroundColor(xc(WHITE)); sCelda.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(sCelda, SLATE_200);

            XSSFCellStyle sCeldaZ = wb.createCellStyle();
            sCeldaZ.setFont(fCelda); sCeldaZ.setVerticalAlignment(VerticalAlignment.CENTER); sCeldaZ.setWrapText(true);
            sCeldaZ.setFillForegroundColor(xc(SLATE_50)); sCeldaZ.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(sCeldaZ, SLATE_200);

            XSSFCellStyle sComentario = wb.createCellStyle();
            sComentario.setFont(fComentario); sComentario.setVerticalAlignment(VerticalAlignment.CENTER); sComentario.setWrapText(true);
            sComentario.setFillForegroundColor(xc(AMBER_100)); sComentario.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(sComentario, AMBER_300);

            // ── Columnas de la tabla de EMPLEADOS: EMPLEADO + tipos de equipo (solo CPU tiene "Nombre") + COMENTARIO ──
            final String[] tiposEmpleado       = {"CPU", "Monitor", "Teléfono IP", "Cámara", "Firma digital", "Lector Optico"};
            final boolean[] tieneNombreEmpleado = {true,  false,     false,         false,     false,           false};

            List<String> colsEmpList = new ArrayList<>();
            colsEmpList.add("EMPLEADO");
            for (int t = 0; t < tiposEmpleado.length; t++) {
                String tipoUpper = tiposEmpleado[t].toUpperCase();
                colsEmpList.add(tipoUpper + " (N/S)");
                if (tieneNombreEmpleado[t]) colsEmpList.add(tipoUpper + " (Nombre)");
            }
            colsEmpList.add("COMENTARIO");
            final int comentarioCol = colsEmpList.size() - 1;
            final int COLS_EMPLEADOS = colsEmpList.size();

            // ── Columnas de la tabla de EQUIPAMIENTO DE OFICINA ──
            final String[] colsOfi = {"TIPO", "NÚMERO DE SERIE", "NOMBRE", "IP"};
            final int COLS_EQUIPO = colsOfi.length;

            final int TOTAL_COLS = Math.max(COLS_EMPLEADOS, COLS_EQUIPO);

            int rowNum = 0;

            Row rT = sheet.createRow(rowNum++); rT.setHeightInPoints(30);
            Cell cT = rT.createCell(0); cT.setCellValue(rel.getNombre()); cT.setCellStyle(sTitulo);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, TOTAL_COLS - 1));

            Row rF = sheet.createRow(rowNum++); rF.setHeightInPoints(15);
            Cell cF = rF.createCell(0);
            cF.setCellValue("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            cF.setCellStyle(sMeta);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, TOTAL_COLS - 1));

            sheet.createRow(rowNum++).setHeightInPoints(8);

            for (Oficina oficina : rel.getOficinas()) {

                // ── Barra de título de la OFICINA (ocupa todo el ancho) ──
                Row rOfi = sheet.createRow(rowNum++); rOfi.setHeightInPoints(24);
                Cell cOfi = rOfi.createCell(0);
                cOfi.setCellValue(oficina.getNombre());
                cOfi.setCellStyle(sOficinaTitulo);
                for (int i = 1; i < TOTAL_COLS; i++) {
                    Cell relleno = rOfi.createCell(i);
                    relleno.setCellStyle(sOficinaTitulo);
                }
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, TOTAL_COLS - 1));

                List<Empleado> empleados = oficina.getEmpleados();
                List<EquipoOficina> equipos = oficina.getEquiposOficina();

                // ── Tabla de EMPLEADOS de esta oficina ──
                if (!empleados.isEmpty()) {
                    Row rHEmp = sheet.createRow(rowNum++); rHEmp.setHeightInPoints(26);
                    for (int i = 0; i < colsEmpList.size(); i++) {
                        Cell cell = rHEmp.createCell(i); cell.setCellValue(colsEmpList.get(i)); cell.setCellStyle(sHeader);
                    }

                    int z = 0;
                    for (Empleado emp : empleados) {
                        Row row = sheet.createRow(rowNum++);
                        row.setHeight((short) -1);
                        XSSFCellStyle est = (z % 2 == 0) ? sCelda : sCeldaZ;

                        Cell cNombre = row.createCell(0);
                        cNombre.setCellValue(emp.getNombre()
                                + (emp.getCargo() != null && !emp.getCargo().isBlank() ? "  ·  " + emp.getCargo() : ""));
                        cNombre.setCellStyle(sEmpleado);

                        int col = 1;
                        for (int t = 0; t < tiposEmpleado.length; t++) {
                            String tipo = tiposEmpleado[t];

                            Cell cSerie = row.createCell(col++);
                            cSerie.setCellValue(buildEquipoSerieText(emp, tipo));
                            cSerie.setCellStyle(est);

                            if (tieneNombreEmpleado[t]) {
                                Cell cNombreEq = row.createCell(col++);
                                cNombreEq.setCellValue(buildEquipoNombreText(emp, tipo));
                                cNombreEq.setCellStyle(est);
                            }
                        }

                        String comentario = emp.getComentario();
                        Cell cCom = row.createCell(comentarioCol);
                        cCom.setCellValue(comentario != null ? comentario : "");
                        cCom.setCellStyle((comentario != null && !comentario.isBlank()) ? sComentario : est);
                        z++;
                    }
                } else {
                    Row rVacio = sheet.createRow(rowNum++);
                    Cell cVacio = rVacio.createCell(0);
                    cVacio.setCellValue("Sin empleados cargados");
                    cVacio.setCellStyle(sMeta);
                }

                // ── Sub-sección EQUIPAMIENTO DE OFICINA (impresoras/escáneres) ──
                if (!equipos.isEmpty()) {
                    sheet.createRow(rowNum++).setHeightInPoints(4);

                    Row rSub = sheet.createRow(rowNum++); rSub.setHeightInPoints(16);
                    Cell cSub = rSub.createCell(0);
                    cSub.setCellValue("Equipamiento de oficina");
                    cSub.setCellStyle(sSubSeccion);
                    for (int i = 1; i < TOTAL_COLS; i++) {
                        Cell relleno = rSub.createCell(i);
                        relleno.setCellStyle(sSubSeccion);
                    }
                    sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, TOTAL_COLS - 1));

                    Row rHOfi = sheet.createRow(rowNum++); rHOfi.setHeightInPoints(20);
                    for (int i = 0; i < colsOfi.length; i++) {
                        Cell cell = rHOfi.createCell(i); cell.setCellValue(colsOfi[i]); cell.setCellStyle(sHeader);
                    }

                    int zo = 0;
                    for (EquipoOficina eq : equipos) {
                        Row row = sheet.createRow(rowNum++);
                        row.setHeight((short) -1);
                        XSSFCellStyle est = (zo % 2 == 0) ? sCelda : sCeldaZ;

                        Cell c0 = row.createCell(0); c0.setCellValue(eq.getTipo()); c0.setCellStyle(est);
                        Cell c1 = row.createCell(1); c1.setCellValue(eq.getNumeroSerie()); c1.setCellStyle(est);
                        Cell c2 = row.createCell(2); c2.setCellValue(eq.getNombre() != null ? eq.getNombre() : ""); c2.setCellStyle(est);
                        Cell c3 = row.createCell(3); c3.setCellValue(eq.getIp() != null ? eq.getIp() : ""); c3.setCellStyle(est);
                        zo++;
                    }
                }

                // ── Separador entre oficinas ──
                sheet.createRow(rowNum++).setHeightInPoints(10);
            }

            for (int i = 0; i < TOTAL_COLS; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1200);
            }
            sheet.setColumnWidth(0, Math.max(sheet.getColumnWidth(0), 5000));
            sheet.setColumnWidth(comentarioCol, Math.max(sheet.getColumnWidth(comentarioCol), 9000));

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private static List<EquipoUsuario> filtrarPorTipo(Empleado emp, String tipo) {
        return emp.getEquipos().stream()
                .filter(e -> tipo.equalsIgnoreCase(e.getTipo()))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Todos los números de serie de un tipo de equipo, uno por línea si hay más de uno. */
    private static String buildEquipoSerieText(Empleado emp, String tipo) {
        List<EquipoUsuario> lista = filtrarPorTipo(emp, tipo);
        if (lista.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            sb.append(lista.get(i).getNumeroSerie());
            if (i < lista.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    /** Todos los nombres de un tipo de equipo, en el mismo orden/línea que buildEquipoSerieText. */
    private static String buildEquipoNombreText(Empleado emp, String tipo) {
        List<EquipoUsuario> lista = filtrarPorTipo(emp, tipo);
        if (lista.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            String n = lista.get(i).getNombre();
            sb.append(n != null ? n : "");
            if (i < lista.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }
}