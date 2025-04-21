package service;

import entite.Examen;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExcelExportService {
    
    private static final Logger LOGGER = Logger.getLogger(ExcelExportService.class.getName());
    
    /**
     * Exporte une liste d'examens vers un document Excel
     * @param examens Liste des examens à exporter
     * @param stage Stage pour l'affichage de la boîte de dialogue
     * @return true si l'exportation a réussi, false sinon
     */
    public boolean exportExamensToExcel(List<Examen> examens, Stage stage) {
        if (examens == null || examens.isEmpty()) {
            LOGGER.warning("La liste d'examens est vide ou null, impossible d'exporter");
            return false;
        }
        
        try {
            // Création de la boîte de dialogue pour sélectionner où enregistrer le fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Documents Excel", "*.xlsx"));
            fileChooser.setInitialFileName("examens.xlsx");
            File file = fileChooser.showSaveDialog(stage);
            
            if (file == null) {
                LOGGER.info("L'utilisateur a annulé l'exportation");
                return false; // L'utilisateur a annulé l'opération
            }
            
            LOGGER.info("Début de l'exportation vers " + file.getAbsolutePath());
            
            // Création du document Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Liste des examens");
            
            // Styles pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Style pour les lignes alternées
            CellStyle altRowStyle = workbook.createCellStyle();
            altRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Création de la ligne d'en-tête
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Titre", "Matière", "Type", "Date", "Durée (min)", "Nb Essais", "Description"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Ajout des données d'examen
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            int rowNum = 1;
            
            for (Examen examen : examens) {
                Row row = sheet.createRow(rowNum++);
                
                // Appliquer le style alterné aux lignes
                if (rowNum % 2 == 0) {
                    for (int i = 0; i < columns.length; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellStyle(altRowStyle);
                    }
                }
                
                // ID
                row.createCell(0).setCellValue(examen.getId());
                
                // Titre
                row.createCell(1).setCellValue(examen.getTitre() != null ? examen.getTitre() : "");
                
                // Matière
                row.createCell(2).setCellValue(examen.getMatiere() != null ? examen.getMatiere() : "");
                
                // Type
                row.createCell(3).setCellValue(examen.getType() != null ? examen.getType() : "");
                
                // Date
                row.createCell(4).setCellValue(examen.getDate() != null ? dateFormat.format(examen.getDate()) : "");
                
                // Durée
                row.createCell(5).setCellValue(examen.getDuree());
                
                // Nb Essais
                row.createCell(6).setCellValue(examen.getNbrEssai());
                
                // Description
                row.createCell(7).setCellValue(examen.getDescription() != null ? examen.getDescription() : "");
            }
            
            // Redimensionnement automatique des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Écriture du fichier
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                LOGGER.info("Exportation terminée avec succès");
            }
            
            workbook.close();
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'exportation vers Excel", e);
            e.printStackTrace();
            return false;
        }
    }
} 