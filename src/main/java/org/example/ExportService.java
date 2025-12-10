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
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row header = sheet.createRow(0);
            String[] columnas = {"ESPERADOS", "ENCONTRADOS", "SOBRANTES"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int maxRows = Math.max(esperados.size(), Math.max(encontrados.size(), sobrantes.size()));
            for (int i = 0; i < maxRows; i++) {
                Row row = sheet.createRow(i + 1);
                if (i < esperados.size()) row.createCell(0).setCellValue(esperados.get(i));
                if (i < encontrados.size()) row.createCell(1).setCellValue(encontrados.get(i));
                if (i < sobrantes.size()) row.createCell(2).setCellValue(sobrantes.get(i));
            }

            for (int i = 0; i < columnas.length; i++) sheet.autoSizeColumn(i);

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

            document.add(new Paragraph("Relevamiento: " + nombre)
                    .setBold().setFontSize(18));
            document.add(new Paragraph("\n"));

            agregarSeccion(document, "Seriales Esperados", esperados);
            agregarSeccion(document, "Seriales Encontrados", encontrados);
            agregarSeccion(document, "Seriales Sobrantes", sobrantes);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void agregarSeccion(Document doc, String titulo, List<String> items) {
        doc.add(new Paragraph(titulo).setBold().setFontSize(14).setMarginTop(10).setMarginBottom(5));

        com.itextpdf.layout.element.List lista = new com.itextpdf.layout.element.List();
        for (String s : items) {
            lista.add(new ListItem(s));
        }
        doc.add(lista);
    }
}
