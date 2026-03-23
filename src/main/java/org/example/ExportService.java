package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

            XSSFFont fSeccion = wb.createFont();
            fSeccion.setFontName("Calibri"); fSeccion.setBold(true);
            fSeccion.setFontHeightInPoints((short) 10); fSeccion.setColor(xc(GREEN_700));

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

            XSSFCellStyle sSeccion = wb.createCellStyle();
            sSeccion.setFont(fSeccion); sSeccion.setVerticalAlignment(VerticalAlignment.CENTER);
            sSeccion.setFillForegroundColor(xc(GREEN_50)); sSeccion.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            sSeccion.setBorderBottom(BorderStyle.THIN); sSeccion.setBottomBorderColor(xc(GREEN_200));

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
            sEmpleado.setBorderLeft(BorderStyle.MEDIUM); sEmpleado.setLeftBorderColor(xc(SLATE_800));

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

            Row rT = sheet.createRow(0); rT.setHeightInPoints(30);
            Cell cT = rT.createCell(0); cT.setCellValue(rel.getNombre()); cT.setCellStyle(sTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            Row rF = sheet.createRow(1); rF.setHeightInPoints(15);
            Cell cF = rF.createCell(0);
            cF.setCellValue("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            cF.setCellStyle(sMeta);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

            sheet.createRow(2).setHeightInPoints(8);

            int rowNum = 3;

            Row rSecEmp = sheet.createRow(rowNum++); rSecEmp.setHeightInPoints(18);
            Cell cSecEmp = rSecEmp.createCell(0);
            cSecEmp.setCellValue("EMPLEADOS"); cSecEmp.setCellStyle(sSeccion);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

            Row rHEmp = sheet.createRow(rowNum++); rHEmp.setHeightInPoints(22);
            String[] colsEmp = {"EMPLEADO", "CPU", "MONITOR", "TELÉFONO IP", "CÁMARA", "FIRMA DIGITAL", "LECTOR ÓPTICO", "COMENTARIO"};
            for (int i = 0; i < colsEmp.length; i++) {
                Cell cell = rHEmp.createCell(i); cell.setCellValue(colsEmp[i]); cell.setCellStyle(sHeader);
            }

            int z = 0;
            for (Empleado emp : rel.getEmpleados()) {
                Row row = sheet.createRow(rowNum++);
                row.setHeight((short)-1); // auto-altura
                XSSFCellStyle est = (z % 2 == 0) ? sCelda : sCeldaZ;

                Cell cNombre = row.createCell(0);
                cNombre.setCellValue(emp.getNombre()
                        + (emp.getCargo() != null && !emp.getCargo().isBlank() ? "  ·  " + emp.getCargo() : ""));
                cNombre.setCellStyle(sEmpleado);

                String[] textos = {
                        buildEquipoText(emp, "CPU", "CPU"),
                        buildEquipoText(emp, "Monitor", "Monitor"),
                        buildEquipoText(emp, "Teléfono IP", "Teléfono"),
                        buildEquipoText(emp, "Cámara", "Cámara"),
                        buildEquipoText(emp, "Firma digital", "Firma"),
                        buildEquipoText(emp, "Lector Optico", "Lector")
                };
                for (int col = 0; col < textos.length; col++) {
                    Cell c = row.createCell(col + 1); c.setCellValue(textos[col]); c.setCellStyle(est);
                }

                String comentario = emp.getComentario();
                Cell cCom = row.createCell(7);
                cCom.setCellValue(comentario != null ? comentario : "");
                cCom.setCellStyle((comentario != null && !comentario.isBlank()) ? sComentario : est);
                z++;
            }

            sheet.createRow(rowNum++).setHeightInPoints(10);

            Row rSecOfi = sheet.createRow(rowNum++); rSecOfi.setHeightInPoints(18);
            Cell cSecOfi = rSecOfi.createCell(0);
            cSecOfi.setCellValue("EQUIPAMIENTO DE OFICINA"); cSecOfi.setCellStyle(sSeccion);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

            Row rHOfi = sheet.createRow(rowNum++); rHOfi.setHeightInPoints(22);
            String[] colsOfi = {"TIPO", "NÚMERO DE SERIE", "NOMBRE"};
            for (int i = 0; i < colsOfi.length; i++) {
                Cell cell = rHOfi.createCell(i); cell.setCellValue(colsOfi[i]); cell.setCellStyle(sHeader);
            }

            int zo = 0;
            for (EquipoOficina eq : rel.getEquiposOficina()) {
                Row row = sheet.createRow(rowNum++);
                row.setHeight((short)-1); // auto-altura
                XSSFCellStyle est = (zo % 2 == 0) ? sCelda : sCeldaZ;
                Cell c0 = row.createCell(0); c0.setCellValue(eq.getTipo()); c0.setCellStyle(est);
                Cell c1 = row.createCell(1); c1.setCellValue(eq.getNumeroSerie()); c1.setCellStyle(est);
                Cell c2 = row.createCell(2); c2.setCellValue(eq.getNombre() != null ? eq.getNombre() : ""); c2.setCellStyle(est);
                zo++;
            }

            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1200);
            }
            sheet.setColumnWidth(0, sheet.getColumnWidth(0) + 2000);
            sheet.setColumnWidth(7, Math.max(sheet.getColumnWidth(7), 9000));

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private static String buildEquipoText(Empleado emp, String tipo, String prefijo) {
        List<EquipoUsuario> lista = emp.getEquipos().stream()
                .filter(e -> tipo.equalsIgnoreCase(e.getTipo()))
                .collect(java.util.stream.Collectors.toList());

        if (lista.isEmpty()) return "";

        if (lista.size() == 1) {
            EquipoUsuario eq = lista.get(0);
            if (eq.getNombre() != null && !eq.getNombre().isEmpty()) {
                return eq.getNumeroSerie() + "\n" + eq.getNombre();
            }
            return eq.getNumeroSerie();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            EquipoUsuario eq = lista.get(i);
            sb.append(prefijo).append(i + 1).append(": ").append(eq.getNumeroSerie());
            if (eq.getNombre() != null && !eq.getNombre().isEmpty()) {
                sb.append("\n  ").append(eq.getNombre());
            }
            if (i < lista.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }
}