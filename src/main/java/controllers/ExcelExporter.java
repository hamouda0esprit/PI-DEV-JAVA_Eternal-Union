package Controllers;

import models.Cours;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    public static void exportToExcel(List<Cours> coursList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Cours");

        // Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "User ID", "Title", "Subject", "Rate", "Last Update"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowIndex = 1;
        for (Cours cours : coursList) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(cours.getId());
            row.createCell(1).setCellValue(cours.getUserId());
            row.createCell(2).setCellValue(cours.getTitle());
            row.createCell(3).setCellValue(cours.getSubject());
            row.createCell(4).setCellValue(cours.getRate());
            row.createCell(5).setCellValue(cours.getLastUpdate().toString());

            // Apply style
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        // Autosize columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream("cours_list.xlsx")) {
            workbook.write(fileOut);
            workbook.close();
            System.out.println("✅ Excel exporté avec succès !");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'exportation Excel : " + e.getMessage());
        }
    }
}
