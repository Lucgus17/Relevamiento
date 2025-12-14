package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.ListItem;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    public static String generarNombreArchivo(String nombre, String extension) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm"));
        return nombre + "(" + fecha + ")." + extension;
    }

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

            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

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

            // ================= HEADERS =================
            Row header = sheet.createRow(3);
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
                Row row = sheet.createRow(i + 4);

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

            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static byte[] generarPdf(
            String nombre,
            List<String> esperados,
            List<String> encontrados,
            List<String> sobrantes
    ) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // ================= TITULO =================

            document.add(new Paragraph(nombre)
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(new Paragraph(
                    "Fecha fin relevamiento: " + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).setFontSize(10)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // ================= TABLA =================
            float[] widths = {1, 1, 1};
            com.itextpdf.layout.element.Table table =
                    new com.itextpdf.layout.element.Table(widths)
                            .useAllAvailableWidth();

            // Headers
            table.addHeaderCell(headerCell("ESPERADOS"));
            table.addHeaderCell(headerCell("ENCONTRADOS"));
            table.addHeaderCell(headerCell("NO INVENTARIADOS"));

            int maxRows = Math.max(
                    esperados.size(),
                    Math.max(encontrados.size(), sobrantes.size())
            );

            for (int i = 0; i < maxRows; i++) {
                table.addCell(bodyCell(i < esperados.size() ? esperados.get(i) : ""));
                table.addCell(bodyCell(i < encontrados.size() ? encontrados.get(i) : ""));
                table.addCell(bodyCell(i < sobrantes.size() ? sobrantes.get(i) : ""));
            }

            document.add(table);
            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static com.itextpdf.layout.element.Cell headerCell(String text) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.DARK_GRAY)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
    }

    private static com.itextpdf.layout.element.Cell bodyCell(String text) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text))
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
    }


    private static void agregarSeccion(Document doc, String titulo, List<String> items) {

        doc.add(new Paragraph(titulo)
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(5));

        if (items.isEmpty()) {
            doc.add(new Paragraph("— Sin registros —").setItalic());
            return;
        }

        com.itextpdf.layout.element.List lista =
                new com.itextpdf.layout.element.List().setSymbolIndent(12);

        for (String s : items) {
            lista.add(new ListItem(s));
        }
        doc.add(lista);
    }

}
