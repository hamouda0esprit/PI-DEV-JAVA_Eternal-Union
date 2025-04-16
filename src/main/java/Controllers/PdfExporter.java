package Controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import models.Cours;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfExporter {

    public static void exportToPdf(List<Cours> coursList) {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream("cours_list.pdf"));
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Paragraph title = new Paragraph("Liste des Cours", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Table
            PdfPTable table = new PdfPTable(6); // 6 columns
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{1f, 1f, 2f, 2f, 1f, 2f});

            // Header style
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            BaseColor headerColor = new BaseColor(224, 224, 224);

            addTableHeader(table, headFont, headerColor, "ID", "User ID", "Title", "Subject", "Rate", "Last Update");

            // Rows
            for (Cours cours : coursList) {
                table.addCell(String.valueOf(cours.getId()));
                table.addCell(String.valueOf(cours.getUserId()));
                table.addCell(cours.getTitle());
                table.addCell(cours.getSubject());
                table.addCell(String.valueOf(cours.getRate()));
                table.addCell(cours.getLastUpdate().toString());
            }

            document.add(table);
            document.close();
            System.out.println("✅ PDF exporté avec succès !");
        } catch (DocumentException | IOException e) {
            System.err.println("❌ Erreur lors de l'exportation PDF : " + e.getMessage());
        }
    }

    private static void addTableHeader(PdfPTable table, Font font, BaseColor background, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(background);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }
}
