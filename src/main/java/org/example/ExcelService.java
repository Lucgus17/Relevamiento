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

            DataFormatter formatter = new DataFormatter(); // <- clave

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(3); // cuarta columna
                    if (cell != null) {

                        String valor = formatter.formatCellValue(cell).trim();

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
}
